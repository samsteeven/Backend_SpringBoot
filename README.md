# EasyPharma Backend - Système de Gestion de Pharmacie

EasyPharma est une application robuste de gestion de pharmacie et de commande de médicaments en ligne, conçue avec Spring Boot.

## 🚀 Fonctionnalités Principales

- **Authentification & Autorisation** : Sécurisation via JWT (Json Web Token).
- **Gestion de Pharmacie** : Inventaire, employés, et statistiques.
- **Recherche de Médicaments** : Recherche intelligente par nom, symptôme ou proximité géographique.
- **Commandes & Livraison** : Gestion du cycle de vie des commandes et assignation des livreurs.
- **Audit & Sécurité** : Logs d'audit détaillés et gestion granulaire des permissions.
- **WebSocket** : Notifications en temps réel.

## 🛠️ Stack Technique

- **Framework** : Spring Boot 3.x
- **Persistance** : Spring Data JPA, Hibernate
- **Base de Données** : PostgreSQL (Prod/Dev), H2 (Test)
- **Migration** : Flyway
- **Sécurité** : Spring Security, JWT
- **Documentation** : Swagger / OpenAPI
- **Tests** : JUnit 5, Mockito, AssertJ

## ⚙️ Configuration & Installation

### Prérequis
- Java 17+
- Maven 3.6+
- PostgreSQL (Optionnel, H2 par défaut en mode dev)

### Exécution
Pour lancer l'application en mode développement :
```bash
./mvnw spring-boot:run
```

### Tests
Pour exécuter la suite de tests (75 tests unitaires et d'intégration) :
```bash
./mvnw test
```

### Documentation API
Une fois lancée, la doc Swagger est accessible à :
`http://localhost:8080/swagger-ui.html`

## 🏗️ Architecture

Le projet suit une architecture propre en couches :
1. **Presentation** : Contrôleurs REST et DTOs.
2. **Application** : Cas d'utilisation (UseCases) et services applicatifs.
3. **Domain** : Entités JPA, agrégats et interfaces de repository (Cœur métier).
4. **Infrastructure** : Sécurité, configuration, implémentations techniques et adaptateurs.

## 📂 Organisation des Dossiers
- `src/main/java` : Code source organisé par couche et par domaine.
- `src/main/resources` : Configuration YAML et migrations SQL (Flyway).
- `src/test` : Tests unitaires et d'intégration robustes.

---
*Projet réalisé dans le cadre du Semestre 1 - EADL.*
"# test" 
