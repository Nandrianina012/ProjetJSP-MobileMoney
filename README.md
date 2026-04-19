# Projet Mobile Money (JSP/Servlet)

Application JSP/Servlet pour gerer:
- CRUD `CLIENT`
- CRUD `FRAIS_ENVOI` et `FRAIS_RECEP`
- Envoi d'argent avec regles de frais
- Retrait avec frais
- Recherche client (`LIKE`)
- Recherche operations par date
- Recette totale operateur
- Export PDF du releve mensuel

## Lancement rapide

1. Creer la base avec `src/main/resources/schema.sql`.
2. Configurer `src/main/resources/db.properties`.
3. (Optionnel) Configurer un SMTP local sur `localhost:25` pour les mails.
4. Compiler:
   - `mvn clean package`
5. Deployer le WAR genere (`target/mobilemoney.war`) dans Tomcat 10+.

## URLs principales

- `/` accueil
- `/clients` gestion clients + recherche LIKE
- `/frais` gestion des tranches de frais
- `/envois` operation d'envoi
- `/retraits` operation de retrait
- `/dashboard` recette + operations par date + lien PDF
- `/reports/statement-pdf?numtel=...&year=2026&month=4` export PDF
