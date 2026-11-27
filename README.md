# Finia

App Android (Jetpack Compose, Hilt, Firebase).

## Flujo de ramas y ambientes (dev / qa / producción)

No hay Jira: el nombre de la rama es el identificador de la tarea.

```
FINIA-002 ──PR──▶ integration ──▶ DEV         Finia DEV  (com.devmastercrack.finia.dev)
                      │
                      PR integration → qa
                      ▼
                     qa        ──▶ QA          Finia QA   (com.devmastercrack.finia.qa)
                      │
                      PR qa → main  (= aprobación para producción)
                      ▼
                     main      ──▶ PRODUCCIÓN  Finia      (com.devmastercrack.finia) + GitHub Release con APK y AAB
```

- **Features**: rama `FINIA-###` (solo el número) creada desde `integration`. Numerar en orden: `FINIA-001`, `FINIA-002`…
- **Hotfix**: rama `hotfix/FINIA-###` desde `main`, PR directo a `main` y después regresar el cambio a `qa` e `integration`.
- Merges siempre como merge commit (sin squash ni rebase).
- **`ci.yml`** corre en cada PR hacia `integration`, `qa` o `main`: valida que el origen respete el flujo y corre tests + compilación.
- **`deploy.yml`** corre en cada push a `integration`, `qa` o `main` (ignora cambios solo de `.md`; también se lanza a mano desde Actions): tests, compila el flavor del ambiente, sube a Firebase App Distribution y, en producción, crea un GitHub Release `v<versionName>-b<run>` con APK y AAB.
- Los tres flavors (`dev`, `qa`, `prod` en `app/build.gradle.kts`) tienen applicationId distinto, así que las tres apps conviven en el mismo celular. `dev` y `qa` se compilan como debug; `prod` como release.
- `versionCode` = número de ejecución de Actions (`FINIA_VERSION_CODE`); `versionName` sale de `defaultConfig` (+ `-dev` / `-qa`). Subir `versionName` antes de promover a producción.

### Configuración en GitHub y Firebase

Proyecto de Firebase: `finia-db203`. `app/google-services.json` trae las tres apps.

| Ambiente (GitHub environment) | Rama permitida | Secretos propios | Firebase App |
|---|---|---|---|
| `dev` | `integration` | `FIREBASE_APP_ID` = `1:906308512787:android:7769410be99022d44923e6` | `com.devmastercrack.finia.dev` |
| `qa` | `qa` | `FIREBASE_APP_ID` = `1:906308512787:android:7bc015a3bc2c2aae4923e6` | `com.devmastercrack.finia.qa` |
| `produccion` | `main` | `FIREBASE_APP_ID` = `1:906308512787:android:1d59f1844ec386274923e6` + `FINIA_KEYSTORE_*` (llave de **release**) | `com.devmastercrack.finia` |

- Secretos a nivel repo (los usan dev y qa): `FINIA_KEYSTORE_BASE64`, `FINIA_KEYSTORE_PASSWORD`, `FINIA_KEY_ALIAS`, `FINIA_KEY_PASSWORD` (llave de **pruebas**) y `FIREBASE_SERVICE_ACCOUNT` (cuenta de servicio con rol *Firebase App Distribution Admin* en `finia-db203`).
- Variable opcional por ambiente `FIREBASE_GROUPS` (por defecto `testers`).
- Copias locales de las llaves fuera del repo, en `C:\Users\atarn\.finia\`. **La llave de release no se puede recuperar si se pierde**: respaldarla.
- Un APK compilado en local está firmado con otra llave: Android no lo instala encima de uno de App Tester (desinstalar primero).
