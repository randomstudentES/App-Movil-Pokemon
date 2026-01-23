package com.example.pokemon_v.ui.composables.api.mappers

object NameTranslator {

    private val paradoxMap = mapOf(
        "great-tusk" to "Colmilargo",
        "scream-tail" to "Colagrito",
        "brute-bonnet" to "Furioseta",
        "flutter-mane" to "Melenaleteo",
        "slither-wing" to "Reptalada",
        "sandy-shocks" to "Pelarena",
        "iron-treads" to "Ferrodada",
        "iron-bundle" to "Ferrosaco",
        "iron-hands" to "Ferropalmas",
        "iron-jugulis" to "Ferrocuello",
        "iron-moth" to "Ferropolilla",
        "iron-thorns" to "Ferropúas",
        "roaring-moon" to "Bramaluna",
        "iron-valiant" to "Ferropaladín",
        "walking-wake" to "Ondulagua",
        "iron-leaves" to "Ferroverdor",
        "gouging-fire" to "Flamariete",
        "raging-bolt" to "Electrofuria",
        "iron-boulder" to "Ferromole",
        "iron-crown" to "Ferrotesta"
    )

    private val specialCaseMap = mapOf(
        "farfetchd" to "Farfetch'd",
        "mr-mime" to "Mr. Mime",
        "mime-jr" to "Mime Jr.",
        "porygon-z" to "Porygon-Z",
        "ho-oh" to "Ho-Oh",
        "type-null" to "Código Cero",
        "tapu-koko" to "Tapu Koko",
        "tapu-lele" to "Tapu Lele",
        "tapu-bulu" to "Tapu Bulu",
        "tapu-fini" to "Tapu Fini",
        "jangmo-o" to "Jangmo-o",
        "hakamo-o" to "Hakamo-o",
        "kommo-o" to "Kommo-o",
        "sirfetchd" to "Sirfetch'd",
        "mr-rime" to "Mr. Rime",
        "wo-chien" to "Wo-Chien",
        "chien-pao" to "Chien-Pao",
        "ting-lu" to "Ting-Lu",
        "chi-yu" to "Chi-yu"
    )

    fun translate(name: String): String {
        paradoxMap[name]?.let { return it }

        specialCaseMap[name]?.let { return it }

        if (name.contains("-")) {
            return name.substringBefore("-").replaceFirstChar { it.uppercase() }
        }

        return name.replaceFirstChar { it.uppercase() }
    }
}
