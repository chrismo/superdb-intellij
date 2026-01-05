# Changelog

All notable changes to the SuperDB IntelliJ plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.51231.1] - 2025-01-04

### Added
- `FILTER` SQL keyword for window function support (e.g., `count(*) FILTER (WHERE ...)`)
- `FLOAT` and `INT` PostgreSQL type aliases

### Changed
- Updated LSP server to v0.51231.1
- Synced grammar with brimdata/super (commit 5ea0cb5d)
