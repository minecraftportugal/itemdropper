config.yml example:

```
mysql:
  enabled: false
  host: localhost
  port: 3306
  db: database
  username: username
  password: password
  table: itemdrop
debug: false
pollDatabase: 1
fallbackCheckPeriod: 5
messages:
  delivered: You have been delivered %s time(s) the item '%s'
  dropped: Your inventory was full, the items were dropped on the ground
```
