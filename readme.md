# Technik & Technologie vernetzter Systeme / Master Informatik
## Projekt: Implementierung eines verteilten Spiels "Schiffe Versenken" (ohne Churn)

Das Ziel des Praktikums ist die Implementierung eines verteilten Spiels. Die Lösung der Aufgabenstellung befindet sich in diesem Repository.

Hierfür wurden die in der Aufgabenstellung genannten Methoden, ChordImpl und NodeImpl, implementiert und um einen asynchronen Versand des Broadcasts erweitert.
Des Weiteren wurde eine graphische Oberfläche entworfen, welche das Spiel visualisiert und das Erstellen und Verbinden zu einem Server ermöglicht. Auf dem ersten Tab wird eine Textausgabe des Spielverlaufs ausgegeben und auf dem zweiten Tab werden die einzelnen Spieler visuell dargestellt.
Abschliessend wurde die Kommunikation mit einem CoAP-Interface implementiert und in den Spielverlauf integriert.

Um das Spielergebnis zu manipulieren wurde ein CheatMode eingebaut, wie dieser aktiviert werden kann ist in einem der Kommentare versteckt! 

Unsere Taktik beim Spielen desteht darin, dass wir immer auf den Spieler mit den wenigsten verbleibenden Schiffen feuern. Sollte noch kein Spieler ein Schiff verloren haben, wird den Spieler in der Liste geschossen, welcher die niedrigste Chord-ID bekommen hat, sollte dies unsere ID sein, wird auf den nachfolgenden Spieler geschossen.  

<b>Hinweis: Das Repository muss als Maven-Paket importiert werden!</b>
