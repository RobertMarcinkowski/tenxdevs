# Dokument wymagań produktu (PRD) - VibeTravels
## 1. Przegląd produktu
VibeTravels to aplikacja w wersji MVP (Minimum Viable Product), której celem jest uproszczenie procesu planowania podróży. Aplikacja wykorzystuje sztuczną inteligencję do przekształcania swobodnych notatek użytkowników w uporządkowane i inspirujące plany wycieczek. Główne funkcjonalności obejmują system kont użytkowników, zarządzanie notatkami podróżniczymi (tworzenie, odczyt, edycja, usuwanie), personalizację poprzez profil preferencji oraz generowanie planów podróży przez AI z uwzględnieniem indywidualnych upodobań. Aplikacja w wersji MVP będzie darmowa.

## 2. Problem użytkownika
Użytkownicy często napotykają trudności podczas planowania podróży, które wynikają z kilku kluczowych problemów. Po pierwsze, brakuje im szczegółowej wiedzy na temat miejsca docelowego, co utrudnia odkrywanie interesujących, często mniej znanych atrakcji. Po drugie, proces wyszukiwania i selekcji miejsc do odwiedzenia jest czasochłonny i przytłaczający. Wreszcie, organizacja logistyki, takiej jak transport czy zakwaterowanie, stanowi dodatkowe wyzwanie. VibeTravels ma na celu rozwiązanie tych problemów, automatyzując tworzenie wstępnych planów i dostarczając spersonalizowane sugestie, co znacząco redukuje wysiłek związany z planowaniem.

## 3. Wymagania funkcjonalne
- 3.1. System kont użytkowników:
    - Użytkownicy muszą mieć możliwość prostej rejestracji i logowania w aplikacji.
    - Każde konto użytkownika jest unikalne i powiązane z jego danymi (notatkami, preferencjami).
- 3.2. Zarządzanie notatkami (CRUD):
    - Użytkownik może tworzyć, przeglądać, edytować i usuwać notatki podróżnicze.
    - Notatki są zapisywane w formie swobodnego, nieustrukturyzowanego tekstu.
- 3.3. Profil użytkownika i preferencje:
    - Dedykowana strona profilu, na której użytkownik może zarządzać swoimi preferencjami.
    - Profil zawiera siedem kategorii preferencji: budżet, tempo podróży, zainteresowania, styl zakwaterowania, rodzaj transportu, jedzenie i sezon podróży.
    - Użytkownik może w dowolnym momencie zaktualizować swoje preferencje.
- 3.4. Generator planów podróży (AI):
    - System generuje plan podróży na podstawie notatki użytkownika oraz jego zapisanych preferencji.
    - Wygenerowany plan ma formę prostej listy atrakcji i aktywności na każdy dzień.
    - Integracja z zewnętrznym dostawcą AI jest realizowana poprzez wymienny klucz API.
    - Wprowadzone są limity użycia generatora AI na jednego użytkownika w celu kontroli kosztów.
- 3.5. System oceniania planów:
    - Użytkownik ma możliwość ocenienia każdego wygenerowanego planu podróży.
    - Oceny są przyznawane w skali od 1 do 5.
- 3.6. Przechowywanie danych:
    - Wszystkie dane (konta, notatki, preferencje, oceny) są przechowywane w prostej bazie danych.

## 4. Granice produktu
Poniższe funkcjonalności nie wchodzą w zakres wersji MVP VibeTravels:
- Współdzielenie planów podróży i notatek między różnymi kontami użytkowników.
- Obsługa i analiza multimediów, takich jak zdjęcia, filmy czy mapy w notatkach lub planach.
- Zaawansowane funkcje planowania logistyki, takie jak rezerwacja transportu, noclegów czy szczegółowe harmonogramowanie czasowe.

## 5. Historyjki użytkowników
### Zarządzanie kontem
- ID: US-001
- Tytuł: Rejestracja nowego użytkownika
- Opis: Jako nowy użytkownik, chcę móc założyć konto w aplikacji, aby móc zapisywać swoje notatki i preferencje.
- Kryteria akceptacji:
    - Użytkownik może przejść do formularza rejestracyjnego.
    - Formularz wymaga podania adresu e-mail i hasła.
    - System sprawdza, czy podany adres e-mail nie jest już zarejestrowany.
    - Po pomyślnym utworzeniu konta, użytkownik jest automatycznie zalogowany i przekierowany na stronę główną.
    - W przypadku błędu (np. zajęty e-mail) wyświetlany jest czytelny komunikat.

- ID: US-002
- Tytuł: Logowanie użytkownika
- Opis: Jako zarejestrowany użytkownik, chcę móc zalogować się na swoje konto, aby uzyskać dostęp do moich notatek i planów.
- Kryteria akceptacji:
    - Użytkownik może wprowadzić e-mail i hasło w formularzu logowania.
    - System weryfikuje poprawność danych uwierzytelniających.
    - Po pomyślnym zalogowaniu, użytkownik jest przekierowywany na stronę główną.
    - W przypadku podania błędnych danych, wyświetlany jest odpowiedni komunikat.

- ID: US-003
- Tytuł: Wylogowanie użytkownika
- Opis: Jako zalogowany użytkownik, chcę móc się wylogować, aby zabezpieczyć dostęp do mojego konta.
- Kryteria akceptacji:
    - W interfejsie aplikacji znajduje się widoczny przycisk "Wyloguj".
    - Po kliknięciu przycisku sesja użytkownika jest kończona, a on sam jest przekierowywany na stronę logowania.

### Zarządzanie profilem i preferencjami
- ID: US-004
- Tytuł: Edycja preferencji podróżniczych
- Opis: Jako użytkownik, chcę móc zdefiniować i zapisać swoje preferencje podróżnicze, aby generowane plany były lepiej dopasowane do moich potrzeb.
- Kryteria akceptacji:
    - W profilu użytkownika znajduje się sekcja do edycji preferencji.
    - Użytkownik może zdefiniować wartości dla siedmiu kategorii: budżet, tempo, zainteresowania, styl zakwaterowania, transport, jedzenie, sezon.
    - Zmiany są zapisywane po kliknięciu przycisku "Zapisz".
    - System potwierdza zapisanie zmian komunikatem.

### Zarządzanie notatkami
- ID: US-005
- Tytuł: Tworzenie nowej notatki
- Opis: Jako użytkownik, chcę móc stworzyć nową notatkę podróżniczą w formie tekstu, aby zapisać pomysł na wycieczkę.
- Kryteria akceptacji:
    - Na stronie głównej lub w dedykowanej sekcji znajduje się przycisk "Dodaj notatkę".
    - Użytkownik może wprowadzić tytuł i treść notatki w polu tekstowym.
    - Po zapisaniu notatka pojawia się na liście notatek użytkownika.

- ID: US-006
- Tytuł: Przeglądanie listy notatek
- Opis: Jako użytkownik, chcę widzieć listę wszystkich moich notatek, aby mieć szybki dostęp do moich pomysłów na podróże.
- Kryteria akceptacji:
    - Lista notatek jest wyświetlana w czytelny sposób (np. tytuł i fragment treści).
    - Kliknięcie na notatkę przenosi do jej szczegółowego widoku.

- ID: US-007
- Tytuł: Edycja istniejącej notatki
- Opis: Jako użytkownik, chcę mieć możliwość edytowania moich notatek, aby zaktualizować informacje o planowanej podróży.
- Kryteria akceptacji:
    - W widoku szczegółowym notatki znajduje się opcja "Edytuj".
    - Użytkownik może zmienić tytuł i treść notatki.
    - Po zapisaniu zmian zaktualizowana treść jest widoczna.

- ID: US-008
- Tytuł: Usuwanie notatki
- Opis: Jako użytkownik, chcę móc usunąć notatkę, której już nie potrzebuję.
- Kryteria akceptacji:
    - W widoku notatki lub na liście znajduje się opcja "Usuń".
    - System prosi o potwierdzenie usunięcia notatki.
    - Po potwierdzeniu notatka jest trwale usuwana z konta użytkownika.

### Generowanie i ocena planu
- ID: US-009
- Tytuł: Generowanie planu podróży
- Opis: Jako użytkownik, chcę wygenerować plan podróży na podstawie mojej notatki, aby otrzymać gotowe propozycje atrakcji i aktywności.
- Kryteria akceptacji:
    - W widoku notatki znajduje się przycisk "Wygeneruj plan".
    - Po kliknięciu system wysyła zapytanie do AI, uwzględniając treść notatki i preferencje użytkownika.
    - Wygenerowany plan jest wyświetlany w formie listy aktywności na każdy dzień.
    - Jeśli użytkownik osiągnął limit użycia AI, wyświetlany jest odpowiedni komunikat, a plan nie jest generowany.

- ID: US-010
- Tytuł: Ocenianie wygenerowanego planu
- Opis: Jako użytkownik, chcę ocenić wygenerowany plan, aby dać systemowi informację zwrotną na temat jego jakości.
- Kryteria akceptacji:
    - Pod każdym wygenerowanym planem znajduje się system oceny w skali 1-5.
    - Użytkownik może wybrać ocenę.
    - Ocena jest zapisywana i powiązana z danym planem.

## 6. Metryki sukcesu
Sukces MVP VibeTravels będzie mierzony za pomocą następujących kluczowych wskaźników:
- 6.1. Adopcja profili użytkowników:
    - Cel: 90% zarejestrowanych użytkowników ma wypełnione co najmniej trzy z siedmiu kategorii preferencji w swoim profilu.
    - Pomiar: Monitorowanie odsetka profili spełniających kryterium "wypełniony".
- 6.2. Zaangażowanie użytkowników:
    - Cel: 75% aktywnych użytkowników generuje 3 lub więcej planów podróży w ciągu roku.
    - Pomiar: Analiza liczby generowanych planów na użytkownika w zadanym okresie.
- 6.3. Jakość generowanych planów:
    - Cel: Średnia ocena generowanych planów utrzymuje się na poziomie 4.0 lub wyższym w skali 1-5.
    - Pomiar: Obliczanie średniej ze wszystkich ocen wystawionych przez użytkowników.
