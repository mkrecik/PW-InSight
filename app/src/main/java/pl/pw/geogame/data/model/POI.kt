package pl.pw.geogame.data.model

data class POI(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

val pois = listOf(
    POI("Cześć!", "Witaj przed Gmachem Głównym Politechniki Warszawskiej! Jak popatrzysz się w górę zobaczysz dwie daty 1899 oraz 1901, które określają początek i koniec budowy gmachu. " +
            "Gmach Główny stanowi sztandarowy przykład monumentalnej architektury późnego historyzmu. Projekt elewacji zawdzięczamy Stefanowi Szyllerowi, który jest uznawany za jednego najwybitniejszych twórców swej epoki. ", 52.22038888550794, 21.01101055773631),
    POI("Wejście lewe", "Twoja przygoda z Gmachem GŁównym właśnie się zaczyna!", 52.220369818738455, 21.010759569475105),
    POI("Wejście prawe", "Twoja przygoda z Gmachem GŁównym właśnie się zaczyna!",52.220457161907596, 21.010830037070555),
    POI("Sala Senatu", "Tu są jakieś obrady", 52.22038774441405, 21.009369350767148),
    POI("Duża Aula", "Stoisz dokładnie na środku Dużej Auli. Popatrz w górę i podziwiaj przeszklony dach i wielopiętrowe krużganki.", 52.22061307730467, 21.010169668546716),
    POI("Biblioteka Główna", "Biblioteka posiada zbiory zarówno w formie drukowanej (ponad 1 000 000 egzemplarzy), jak i elektronicznej (ponad 180 000 tytułów). Dyrektorem biblioteki jest Alicja Portacha. Projektanci nadbudowy Biblioteki Głównej PW – Hanna Gutkiewicz-Czajkowska i Sławomir Czajkowski – uzyskali nominacje do nagrody głównej w kategorii budynków użyteczności publicznej w konkursie na najlepsze realizacje architektoniczne Warszawy 1998–1999.",52.22073429529581, 21.00973427914728),
    POI("Prawy dziedziniec", "Tutaj palą jednorazówki.", 52.22089525522792, 21.009916946321965),
    POI("Lewy dziedziniec", "Tutaj też palą jednorazówki.", 52.22052727234854, 21.00962629989077),
    )

