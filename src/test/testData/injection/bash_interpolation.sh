#!/bin/bash
# Test bash variable interpolation in super commands

# Type declaration via variable
owned_item_v2_type='type owned_item_v2 = {id: string, qty: int64, archive: bool}'

# Bash interpolation followed by where (the variable contains a type declaration)
total=$(_current_owned_items |
    super -f line -c "$owned_item_v2_type
      where item_id == '$item_id' and archive == false
      | sum(qty)" -)

# Multiple interpolations as array elements
local -r discord_component_json=$(super -j -c "
    [
      {type:10, content:'\`\`\`\n${ j_newline_only_esc "$stats"; }\`\`\`'},
      {type:10, content:'\`\`\`\n${ j_newline_only_esc "$maze_ascii"; }\n\`\`\`'},
      {
       type:1,
       components: [
          {type:2, label:'1', style:1, custom_id:'trident 1'},
          {type:2, label:'2', style:1, custom_id:'trident 2'},
          {type:2, label:'3', style:1, custom_id:'trident 3'}
       ]
      },
      ${ _discord_trident_prev_response "$prev_result"; },
      ${ _maze_context_components; },
      ${ _discord_trident_achievement "$achievement"; }
    ] | unnest this | where this is not null | collect(this)")

# Simple variable substitution in expressions
super -c "where name == '$USER'"
super -c "from data | where count > $threshold"

# Type variable followed by assignment and pipe
echo "$existing_item" |
    super -s -c "$owned_item_v2_type
      qty:=$new_qty,ts:=now()
      | cast(this, <owned_item_v2>)" - >>"$output_file"
