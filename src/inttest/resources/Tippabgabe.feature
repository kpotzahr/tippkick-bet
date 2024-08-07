@cleanData
Feature: Tipp abgeben

  Background:
    Given Abgegebene Tipps:
      | tipper  | spiel                   | ergebnis |
      | Ronaldo | Deutschland-Brasilien   | 1:0      |
      | Ronaldo | Argentinien-Niederlande | 1:1      |
      | Ich     | Argentinien-Niederlande | 3:0      |
    And "Deutschland-Brasilien" wurde noch nicht gestartet
    And "Argentinien-Niederlande" wurde bereits gestartet

  Scenario: Tipp erfolgreich abgeben
    When  ich tippe für "Deutschland-Brasilien" 7:1
    Then  mein gültiger Tipp ist 7:1

  @skip
  Scenario: Tipp erfolgreich ändern
    When  ich tippe für "Deutschland-Brasilien" 2:1
    And   ich tippe für "Deutschland-Brasilien" 0:2
    Then  mein gültiger Tipp ist 0:2

  @event
  Scenario: Tippänderung zu spät abgeben
    When  ich tippe für "Argentinien-Niederlande" 1:0
    Then  der Tipp wird nicht angenommen
    And   mein gültiger Tipp ist 3:0
    And   es wurde kein Tipp bekanntgegeben

  @event
  Scenario: Endgültigen Tipp bekannt geben
    Given  ich tippe für "Deutschland-Brasilien" 7:1
    When   das Spiel "Deutschland-Brasilien" wird gestartet
    Then  Folgende Tipps werden bekanntgegeben:
      | tipper  | spiel                 | ergebnis |
      | Ronaldo | Deutschland-Brasilien | 1:0      |
      | Ich     | Deutschland-Brasilien | 7:1      |

  @skip
  Scenario: Robustheit: Tipp erfolgreich abgeben, wenn Spielplan Service nicht verfügbar
    Given Spielplan ist nicht verfügbar
    When  ich tippe für "Deutschland-Brasilien" 0:0
    Then  mein gültiger Tipp ist 0:0
