I NEED YOU TO USE THE TOOLS YOU HAVE AVAILABLE TO YOU. DO NOT IMPROVISE NEW
BASH SCRIPTS.

For anything with SuperDB, use ./scripts/claude-superdb-test.sh and refer to
https://raw.githubusercontent.com/chrismo/superkit/refs/heads/main/doc/superdb-expert.md
for how to use `super` and compose commands/queries.

command with no input: ./scripts/claude-superdb-test.sh 'values 1'
command with std input: ./scripts/claude-superdb-test.sh 'values this' '1'
command with data file: ./scripts/claude-superdb-test.sh 'values this' examples/data.json
command with an include file: ./scripts/claude-superdb-test.sh examples/basic_read_data.spq examples/data.json 
command with an include file AND cmd: ./scripts/claude-superdb-test.sh examples/basic_include.spq 'my_values(42)' ''
command with include, cmd, AND data file: ./scripts/claude-superdb-test.sh examples/basic_include.spq 'my_values(this.a)' examples/data.json

For working with the tests in this repo, use scripts/analyze-tests.sh. If it needs 
enhancing, propose an enhancement to it.