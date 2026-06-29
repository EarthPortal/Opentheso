# SSO Gaia Data (Keycloak / OIDC) pour OpenTheso

Intégration de l'authentification unique EarthPortal ⇄ OpenTheso via le SSO Gaia Data.

## Côté Keycloak (SSO Gaia Data)

Client OIDC déjà créé par l'équipe IPSL :

- **client-id** : `earthdata-earthportal-opentheso`
- **issuer-uri** : `https://sso.earth-data.fr/realms/gaia-data`
- **client-secret** : fourni hors-bande (ne jamais committer)

À faire enregistrer côté client Keycloak (sinon échec `invalid redirect_uri`) :

- **Valid redirect URIs** : `https://<host-opentheso>/login/oauth2/code/keycloak`
  (attention au `context-path` : si OpenTheso est servi sous `/opentheso`,
  l'URI devient `https://<host>/opentheso/login/oauth2/code/keycloak`)
- **Post-logout redirect URI** : `https://<host-opentheso>/`
- **Web origins** : `https://<host-opentheso>`
- **scope** : le scope `roles` doit être inclus pour faire remonter
  `resource_access.<client-id>.roles` dans le token.

## Côté OpenTheso (variables d'environnement)

| Variable             | Rôle                                              | Défaut                                              |
|----------------------|---------------------------------------------------|-----------------------------------------------------|
| `KEYCLOAK_ENABLED`   | active le SSO (`true`) ou l'auth locale (`false`) | `false`                                             |
| `OIDC_CLIENT_ID`     | client-id OIDC                                    | `earthdata-earthportal-opentheso`                   |
| `OIDC_CLIENT_SECRET` | secret du client (**obligatoire si SSO activé**)  | _(vide)_                                            |
| `OIDC_ISSUER_URI`    | realm Keycloak                                    | `https://sso.earth-data.fr/realms/gaia-data`        |

> L'`issuer-uri` doit être joignable au démarrage de l'application (découverte OIDC).

## Rôles

Le mapping lit en priorité les **rôles du client** dans le claim
`resource_access.<OIDC_CLIENT_ID>.roles` (recommandation de l'équipe SSO Gaia Data),
avec un **fallback** sur `realm_access.roles`. Chaque rôle Keycloak `x` devient
l'autorité Spring `ROLE_X`.

Le rattachement à un compte OpenTheso se fait **par e-mail** : après connexion SSO,
l'utilisateur doit déjà exister dans la base OpenTheso (même e-mail), sinon le
message « vous n'avez pas de compte dans Opentheso » s'affiche.

## Tester

1. Vérifier l'issuer : `curl -s $OIDC_ISSUER_URI/.well-known/openid-configuration | jq .issuer`
2. Lancer avec `KEYCLOAK_ENABLED=true` + `OIDC_CLIENT_SECRET=…`.
3. Page de login → bouton **SSO…** → redirection vers `sso.earth-data.fr` → retour connecté.
