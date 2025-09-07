# serve

minimal resource pack web server for minecraft.  
serves a `.zip` resource pack over http so you can set `server.properties` to host packs directly from your server.

---

## features

- serves any `.zip` file as a resource pack
- calculates SHA-1 automatically for `server.properties`
- live reload via `/serve reload`
- configurable port and bind address

---

## usage

1. drop `serve.jar` in your `plugins` folder  
2. place your resource pack in the plugin folder or specify a path in `config.yml`  
3. start the server  
4. check console for suggested `server.properties` lines  

reload config without restarting server:  
```

/serve reload

````

---

## config (default)

```yaml
file: "plugins/ModelEngine/resource pack.zip"
port: 8081
bind-address: "0.0.0.0"
````

---

## notes

* plugin logs errors if file is missing or server fails to start
* SHA-1 hash is optional but recommended for clients
* requires java 8+ and a Bukkit/Spigot/Paper server

---

## license

[MIT](LICENSE)
