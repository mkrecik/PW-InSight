package pl.pw.geogame.data.model

data class POI(
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

val pois = listOf(
    POI("Cześć!",
        "Witaj przed Gmachem Głównym Politechniki Warszawskiej! Jak popatrzysz się w górę zobaczysz dwie daty 1899 oraz 1901, które określają początek i koniec budowy gmachu. " +
            "Gmach Główny stanowi sztandarowy przykład monumentalnej architektury późnego historyzmu. Projekt elewacji zawdzięczamy Stefanowi Szyllerowi, który jest uznawany za jednego najwybitniejszych twórców swej epoki. ",
        52.22038888550794, 21.01101055773631),
    POI("Wejście lewe",
        "Twoja przygoda z Gmachem GŁównym właśnie się zaczyna!",
        52.220369818738455, 21.010759569475105),
    POI("Wejście prawe",
        "Twoja przygoda z Gmachem GŁównym właśnie się zaczyna!",
        52.220457161907596, 21.010830037070555),
    POI("Sala Senatu",
        "Tu są jakieś obrady",
        52.22038774441405, 21.009369350767148),
    POI("Duża Aula",
        "Stoisz dokładnie na środku Dużej Auli. Popatrz w górę i podziwiaj przeszklony dach i wielopiętrowe krużganki.",
        52.22054274760066, 21.010280349218043),
    POI("Biblioteka Główna",
        "Biblioteka posiada zbiory zarówno w formie drukowanej (ponad 1 000 000 egzemplarzy), jak i elektronicznej (ponad 180 000 tytułów). Dyrektorem biblioteki jest Alicja Portacha. Projektanci nadbudowy Biblioteki Głównej PW – Hanna Gutkiewicz-Czajkowska i Sławomir Czajkowski – uzyskali nominacje do nagrody głównej w kategorii budynków użyteczności publicznej w konkursie na najlepsze realizacje architektoniczne Warszawy 1998–1999.",
        52.22073429529581, 21.00973427914728),
    POI("Północny dziedziniec",
        "Z północnego dziedzinca masz wgląd na bibliotekę główną jak i na szklaną windę, która zaprowadzi Cię aż na 4 piętro.",
        52.22089525522792, 21.009916946321965),
    POI("Południowy dziedziniec",
        "Z południowego dziedzinca możesz podziwiać piękną architekturę, która odnosi się do stylistyki włoskiego renesansu i baroku.",
        52.22052727234854, 21.00962629989077),
    POI("Pomnik Eugeniusza Kwiatkowskiego",
        "Eugeniusz Felicjan Kwiatkowski - polski chemik, wicepremier, minister przemysłu i handlu (1926–1930), minister skarbu (1935–1939) II Rzeczypospolitej.",
        52.22059923225709, 21.010452611599526),
    POI("Pomnik Ignacego Mościckiego",
        "Ignacy Mościcki - polski chemik, polityk, prezydent II RP w latach 1926-1939. W latach 1912-1922 profesor Politechniki Lwowskiej, jak i autor nowatorskiej metody pozyskiwania kwasu azotowego z powietrza. Profesor Politechniki Warszawskiej w latach 1925-1926.",
        52.22046989641635, 21.010334734593904),
    POI("Pomnik  Marii Skłodowskiej-Curie",
        "Maria Skłodowksa-Curie - lauratka nagrody nobla w latach 1903 z fizyki i 1911 z chemii. Pomnik został odsłonięty w 2005 roku dla upamiętnienia nadania noblistce doktoratu honoris causa Politechniki Warszawskiej w 1926 roku. Jej uniesiona prawa ręka symbolizuje świadomość posiadanej wiedzy (mudra z filozofii indyjskiej)",
        52.22055369890026, 21.010495667125067),
    POI("Tabliczka informacyjna",
        "To właśnie 4 stycznia 1826 otwarto w Warszawie pierwszą Politechnikę! Pierwszym rektorem był Kajetan Garbiński, a prezesem rady politechnicznej był wtedy Stanisław Staszic.",
        52.220408439773756, 21.0100613342607)
    )

