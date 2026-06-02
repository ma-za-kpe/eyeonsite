package com.maku.eyeonsite.ui.auth

data class Country(
    val name: String,
    val isoCode: String,
    val dialCode: String,
    val flag: String,
)

val defaultCountry = Country("Ghana", "GH", "233", "🇬🇭")

val allCountries: List<Country> =
    listOf(
        // Primary African markets
        defaultCountry,
        Country("Nigeria", "NG", "234", "🇳🇬"),
        Country("Kenya", "KE", "254", "🇰🇪"),
        Country("South Africa", "ZA", "27", "🇿🇦"),
        Country("Uganda", "UG", "256", "🇺🇬"),
        Country("Tanzania", "TZ", "255", "🇹🇿"),
        Country("Rwanda", "RW", "250", "🇷🇼"),
        Country("Ethiopia", "ET", "251", "🇪🇹"),
        Country("Senegal", "SN", "221", "🇸🇳"),
        Country("Côte d'Ivoire", "CI", "225", "🇨🇮"),
        Country("Cameroon", "CM", "237", "🇨🇲"),
        Country("Egypt", "EG", "20", "🇪🇬"),
        Country("Morocco", "MA", "212", "🇲🇦"),
        // Diaspora markets
        Country("Australia", "AU", "61", "🇦🇺"),
        Country("Brazil", "BR", "55", "🇧🇷"),
        Country("Canada", "CA", "1", "🇨🇦"),
        Country("China", "CN", "86", "🇨🇳"),
        Country("Denmark", "DK", "45", "🇩🇰"),
        Country("France", "FR", "33", "🇫🇷"),
        Country("Germany", "DE", "49", "🇩🇪"),
        Country("India", "IN", "91", "🇮🇳"),
        Country("Italy", "IT", "39", "🇮🇹"),
        Country("Japan", "JP", "81", "🇯🇵"),
        Country("Netherlands", "NL", "31", "🇳🇱"),
        Country("Norway", "NO", "47", "🇳🇴"),
        Country("Portugal", "PT", "351", "🇵🇹"),
        Country("Spain", "ES", "34", "🇪🇸"),
        Country("Sweden", "SE", "46", "🇸🇪"),
        Country("United Arab Emirates", "AE", "971", "🇦🇪"),
        Country("United Kingdom", "GB", "44", "🇬🇧"),
        Country("United States", "US", "1", "🇺🇸"),
    )
