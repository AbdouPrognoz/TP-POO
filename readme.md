# ESI - Smart Farming
**POO – Travail Pratique | École Nationale Supérieure d'Informatique (ESI-Alger) | 2CP | 2025/2026**

---

## Énoncé

Une exploitation agricole souhaite concevoir et implémenter une application de bureau pour la gestion intelligente de sa ferme. La ferme est organisée en zones géographiques, chacune identifiée par un code unique et un nom. On distingue trois types de zones, chacune dédiée à un type d'entité spécifique et disposant de ses propres caractéristiques et capteurs :

- Une **zone de culture** accueille des cultures et est équipée de capteurs environnementaux et de capteurs de sol.
- Une **zone d'élevage** accueille des animaux équipés de capteurs biométriques et de colliers GPS.
- Une **zone aquacole** accueille des espèces aquacoles dans un bassin dont les paramètres de l'eau sont surveillés par des capteurs dédiés.

Quelle que soit sa nature, une zone peut être **active** ou **suspendue** pour maintenance. La suspension d'une zone entraîne automatiquement la suspension de tous les capteurs qu'elle contient ; la réactivation de la zone les remet en service.

---

### Zones de Culture

Les cultures sont regroupées en trois familles :
- Les **céréales** (blé, maïs, orge, etc.)
- Les **légumes** (tomate, pomme de terre, carotte, etc.)
- Les **fruits** (pomme, raisin, olive, etc.)

Chaque culture est caractérisée par :
- Sa date de plantation
- Sa date de récolte prévue
- Son stade de croissance actuel (semis, germination, croissance, maturité, récolte)
- Ses exigences pédologiques (plage optimale de pH et d'humidité du sol)

Les conditions de croissance de chaque culture sont surveillées en continu par des **capteurs environnementaux** (température, humidité, pluviométrie) et des **capteurs de sol** (pH, taux d'humidité, teneur en azote).

Chaque zone de culture définit un programme d'alimentation précisant le type d'aliment et les quantités par repas.

---

### Zones d'Élevage

Une zone d'élevage accueille soit des **ruminants** (vaches, moutons, chèvres, etc.) soit de la **volaille** (poulets, dindes, etc.). Chaque animal est suivi par des **capteurs biométriques** enregistrant sa température corporelle et son niveau d'activité en pas par minute. Il peut être équipé d'un **collier GPS** qui envoie périodiquement sa position géographique (latitude et longitude) à l'application.

Une alerte est déclenchée si l'animal quitte les limites géographiques de sa zone assignée.

Chaque animal est identifié par un numéro unique et est caractérisé par :
- Son espèce
- Son âge
- Son poids
- Son état de santé (sain, malade ou en quarantaine)

Chaque zone d'élevage définit un programme d'alimentation précisant le type d'aliment et les quantités par repas.

---

### Zones Aquacoles

Une zone aquacole contient un bassin équipé de **capteurs d'eau** (température, oxygène dissous et pH) et accueille des espèces aquacoles (poissons, crevettes, etc.). Elle est caractérisée par le nombre d'animaux, leur espèce et un programme d'alimentation précisant le type d'aliment et les quantités par repas.

---

### Historique de Production

Chaque zone est associée à un historique de production :
- Rendement laitier pour les zones de ruminants
- Production d'œufs pour les zones de volaille
- Poids de récolte pour les zones aquacoles
- Rendement des cultures pour les zones de culture

---

### Capteurs

Chaque capteur est identifié par un code unique, est localisé dans une zone, possède un statut (**actif**, **défaillant** ou **suspendu**) et une plage de seuils (valeur minimale et maximale acceptables).

- La suspension d'un capteur le désactive et l'empêche d'envoyer des relevés à l'application ; il peut être réactivé une fois la maintenance terminée.
- Les **capteurs GPS** envoient la position géographique de l'animal sous forme de coordonnées.
- Tous les autres capteurs envoient une valeur numérique unique accompagnée de son unité de mesure.
- Chaque capteur envoie périodiquement ses relevés à l'application.

Lorsqu'une valeur relevée sort de la plage de seuils configurée, le système génère automatiquement une **alerte**. Chaque alerte est associée au relevé qui l'a déclenchée et est caractérisée par un **niveau de gravité** (avertissement ou critique).

Les alertes sont affichées dans un panneau dédié et peuvent être acquittées ou supprimées par le gestionnaire de la ferme. Un historique complet de toutes les alertes est conservé et consultable. De plus, l'application propose un **module de visualisation graphique** qui affiche l'évolution des relevés au fil du temps sous forme de graphiques, avec des indicateurs colorés reflétant le niveau de gravité de chaque relevé (normal, avertissement ou critique).

---

## Fonctionnalités du Système

### 1. Gérer les Zones et les Entités de la Ferme
- Ajouter, modifier ou désactiver une zone.
- Affecter des cultures ou des animaux à une zone.
- Afficher une vue d'ensemble de toutes les zones avec leur statut et le nombre d'entités hébergées.
- Enregistrer la production de chaque zone.

### 2. Gérer les Cultures
- Enregistrer une culture avec son type, ses dates de plantation et de récolte, et ses exigences pédologiques.
- Mettre à jour et afficher le stade de croissance de chaque culture.
- Générer un rapport de l'état des cultures par zone.

### 3. Gérer les Animaux
- Enregistrer un animal avec son espèce, son âge, son poids et son état de santé.
- Consigner les événements sanitaires : maladies et évolutions de poids.
- Définir et afficher les programmes d'alimentation par zone.

### 4. Gérer les Capteurs
- Ajouter et configurer un capteur : type, zone et plage de seuils (valeur min/max).
- Afficher un tableau de bord des relevés par zone avec des indicateurs colorés (normal / avertissement / critique).
- Consulter l'historique des relevés d'un capteur, filtrable par plage de dates.
- Changer le statut d'un capteur (actif, défaillant ou suspendu).
- Afficher des graphiques de l'évolution des relevés par capteur ou par zone.

### 5. Gérer le Système d'Alertes
- Déclencher automatiquement une alerte lorsqu'un relevé dépasse les seuils configurés.
- Afficher le panneau des alertes actives, triées par niveau de gravité (critique en premier).
- Acquitter ou supprimer une alerte.
- Consulter l'historique des alertes, filtrable par zone, type de capteur, niveau et période.