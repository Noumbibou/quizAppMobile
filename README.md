# 🚀 Quiz App Pro - Système de Quiz Sécurisé avec IA

**Quiz App Pro** est une application Android native développée en Java, conçue pour offrir une expérience d'examen sécurisée et moderne. Elle intègre des technologies d'intelligence artificielle pour la détection de fraude et une architecture robuste connectée à un backend FastAPI.

---

## 📸 Fonctionnalités Clés

### 👤 Espace Utilisateur
- **Authentification Sécurisée** : Connexion et inscription via Firebase Auth.
- **Sélection de Niveaux** : Trois niveaux de difficulté (Débutant, Intermédiaire, Avancé) avec 50 questions chacun.
- **Quiz Interactif** : Interface fluide avec compte à rebours de 20 secondes par question.
- **Leaderboard Dynamique** : Classement en temps réel affichant les vrais noms des utilisateurs.
- **Statistiques Avancées** : Graphiques de progression (LineChart) via MPAndroidChart basés sur l'historique réel.
- **Localisation** : Visualisation de la position de l'utilisateur sur Google Maps via une interface dédiée.

### 🛡️ Système Anti-Triche (IA & Cycle de vie)
- **Surveillance ML Kit** : Détection de visage en temps réel. L'examen s'arrête si l'utilisateur quitte l'écran ou si plusieurs visages sont détectés.
- **Protection Focus** : Détection immédiate du changement d'application, de l'ouverture du volet de notifications ou du mode écran scindé.
- **Fenêtre Sécurisée** : Blocage des captures d'écran et des enregistrements vidéo (`FLAG_SECURE`).

### 🔑 Espace Administration
- **Gestion des Utilisateurs** : Visualisation et contrôle des comptes.
- **Gestion des Questions** : Interface CRUD pour ajouter, modifier ou supprimer des questions par set.
- **Rapports de Fraude** : Historique détaillé des tentatives de triche détectées par l'IA.

---

## 🛠️ Stack Technique

- **Langage** : Java
- **UI/UX** : Material Design 3 (Material Components)
- **Backend** : FastAPI (Python)
- **Authentification** : Firebase Auth
- **IA** : Google ML Kit (Face Detection)
- **Cartographie** : Google Maps SDK
- **Réseau** : Retrofit 2 & OkHttp
- **Graphiques** : MPAndroidChart

---

## 🚀 Installation et Configuration

### 1. Prérequis
- Android Studio Ladybug (ou version récente)
- Clé API Google Maps (à placer dans `AndroidManifest.xml`)
- Backend FastAPI opérationnel

### 2. Connexion au Backend (Développement)
Si vous utilisez un téléphone réel en USB, utilisez la commande suivante pour lier les ports :
```bash
adb reverse tcp:8000 tcp:8000
```
L'URL de base dans `ApiClient.java` doit être `http://127.0.0.1:8000/`.

### 3. Firebase
- Ajoutez votre fichier `google-services.json` dans le dossier `app/`.
- Activez l'authentification par Email/Mot de passe dans la console Firebase.

---

## 🧠 Principe de Surveillance IA

L'application utilise un flux `ImageAnalysis` de **CameraX** lié à **ML Kit** :
1. **Initialisation** : Le détecteur de visage est configuré en mode `PERFORMANCE_MODE_FAST`.
2. **Analyse** : Chaque frame est analysée pour compter les visages et vérifier la position du nez par rapport au centre de l'écran.
3. **Tolérance** : Un compteur d'anomalies est implémenté. Après 3 détections négatives consécutives (absence, multiple visages ou mouvement brusque), une alerte de fraude est déclenchée.

---

## 👥 Auteurs
- **Projet** : QuizApp_Fomin_G2Roudani
- **Développeur** : [Votre Nom / Équipe]

---
*Ce projet a été réalisé dans le cadre du module Développement Mobile.*
