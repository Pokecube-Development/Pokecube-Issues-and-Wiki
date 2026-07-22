package pokecube.gimmicks.mega;

import java.util.Map;

import pokecube.api.data.PokedexEntry;

/**
 * Canonical Pokemon Legends: Z-A names and tint colours for Mega Stones. The colour order matches the four item model
 * layers: outline, inner detail, secondary accent, and primary ring.
 */
public final class MegaStoneColours
{
    private record Stone(String name, int outline, int detail, int secondary, int primary)
    {
        int[] toArray()
        {
            return new int[] { this.outline, this.detail, this.secondary, this.primary };
        }
    }

    /*
     * Sampled from the common Pokemon Legends: Z-A bag-sprite layout at (100,80), (90,90), (90,80), and (110,80).
     * This index supersedes the deprecated individual-item Mega Stone sprites.
     */
    private static final Map<String, Stone> STONES = Map.ofEntries(
            Map.entry("abomasnow-mega", stone("Abomasite", 0xFFBAD7CF, 0xFF3C9CA7, 0xFFFDFDFD, 0xFF84AEDF)),
            Map.entry("absol-mega", stone("Absolite", 0xFF698BB4, 0xFF315B91, 0xFFE6EEF1, 0xFFB3CDEA)),
            Map.entry("aerodactyl-mega", stone("Aerodactylite", 0xFF5B166D, 0xFF6243A4, 0xFF47494B, 0xFFBAB2D3)),
            Map.entry("aggron-mega", stone("Aggronite", 0xFF918E8F, 0xFFD1D1C7, 0xFF646163, 0xFFC8D4EE)),
            Map.entry("alakazam-mega", stone("Alakazite", 0xFFCB92A0, 0xFFE4D967, 0xFF843553, 0xFFD2E1D1)),
            Map.entry("altaria-mega", stone("Altarianite", 0xFF97E0FE, 0xFFE7E7E7, 0xFF69D5FF, 0xFFEDAADD)),
            Map.entry("ampharos-mega", stone("Ampharosite", 0xFFFF6E6A, 0xFFFDDF4B, 0xFFED1A24, 0xFFB3CFEE)),
            Map.entry("audino-mega", stone("Audinite", 0xFFE7A4A6, 0xFFFFF7D6, 0xFFEE6A8C, 0xFFFFDEE8)),
            Map.entry("banette-mega", stone("Banettite", 0xFFF49A2F, 0xFF646263, 0xFFF7EC6E, 0xFFF6ADCC)),
            Map.entry("beedrill-mega", stone("Beedrillite", 0xFFCCA208, 0xFF393939, 0xFFF8C719, 0xFFC4BCE6)),
            Map.entry("blastoise-mega", stone("Blastoisinite", 0xFF44B2D1, 0xFF4B96CD, 0xFF624A28, 0xFFD3D2C7)),
            Map.entry("blaziken-mega", stone("Blazikenite", 0xFF88280F, 0xFFEF2F2C, 0xFF221F20, 0xFFFDF1EC)),
            Map.entry("camerupt-mega", stone("Cameruptite", 0xFFC65A48, 0xFF4B4A44, 0xFFEC7159, 0xFFBBAC9C)),
            Map.entry("charizard-mega-x", stone("Charizardite X", 0xFF22618C, 0xFF484A4D, 0xFF45BDE8, 0xFF6EC2EC)),
            Map.entry("charizard-mega-y", stone("Charizardite Y", 0xFFFE9A15, 0xFFED1A2E, 0xFFE6EC21, 0xFFF68648)),
            Map.entry("diancie-mega", stone("Diancite", 0xFFE0B572, 0xFFE68AAB, 0xFFF9EFA5, 0xFFF4E4E5)),
            Map.entry("gallade-mega", stone("Galladite", 0xFFDA99A2, 0xFF59A392, 0xFFDB6273, 0xFFEAEBD9)),
            Map.entry("garchomp-mega", stone("Garchompite", 0xFFF78613, 0xFFEF2F2C, 0xFFFAB30A, 0xFF444868)),
            Map.entry("gardevoir-mega", stone("Gardevoirite", 0xFFD93670, 0xFF94D87C, 0xFFF263AF, 0xFFB3CDEB)),
            Map.entry("gengar-mega", stone("Gengarite", 0xFFC1144C, 0xFF493D61, 0xFFEA1F3B, 0xFFA3A9CF)),
            Map.entry("glalie-mega", stone("Glalitite", 0xFF3A6070, 0xFF499ABA, 0xFF3B3B3A, 0xFFD4DAEC)),
            Map.entry("gyarados-mega", stone("Gyaradosite", 0xFF821976, 0xFF3286CE, 0xFFD7144B, 0xFFCA922C)),
            Map.entry("heracross-mega", stone("Heracronite", 0xFFEA1F3A, 0xFF27598F, 0xFFF38351, 0xFFF9DB3A)),
            Map.entry("houndoom-mega", stone("Houndoominite", 0xFFBF1C2C, 0xFF303D39, 0xFFEB2034, 0xFFA25E7E)),
            Map.entry("kangaskhan-mega", stone("Kangaskhanite", 0xFFA57F9F, 0xFFA3A9CE, 0xFF515758, 0xFFF8DE82)),
            Map.entry("latias-mega", stone("Latiasite", 0xFFC6509B, 0xFF8B63D5, 0xFFFF3138, 0xFFBEC6FB)),
            Map.entry("latios-mega", stone("Latiosite", 0xFF5E73EB, 0xFF8B63D6, 0xFF2884F7, 0xFFBDC6FC)),
            Map.entry("lopunny-mega", stone("Lopunnite", 0xFF6A423C, 0xFFA16355, 0xFF493A3B, 0xFFF9F4BD)),
            Map.entry("lucario-mega", stone("Lucarionite", 0xFF752763, 0xFF067891, 0xFFB00D2E, 0xFFF68536)),
            Map.entry("manectric-mega", stone("Manectite", 0xFFA5185A, 0xFF4EB1CB, 0xFFEB1F33, 0xFFEDF588)),
            Map.entry("mawile-mega", stone("Mawilite", 0xFFB47226, 0xFF4F4C4E, 0xFFF9ED9A, 0xFFF595CB)),
            Map.entry("medicham-mega", stone("Medichamite", 0xFFEE702E, 0xFFEF3494, 0xFFF8E082, 0xFF6FC1ED)),
            Map.entry("metagross-mega", stone("Metagrossite", 0xFFB7B3B9, 0xFFDA9B5B, 0xFFCCCCCC, 0xFF86BCD4)),
            Map.entry("mewtwo-mega-x", stone("Mewtwonite X", 0xFF27588F, 0xFF8F6CA0, 0xFF2E82B2, 0xFFD1D1D2)),
            Map.entry("mewtwo-mega-y", stone("Mewtwonite Y", 0xFFB479C2, 0xFF8F6CA0, 0xFFE2C2E6, 0xFFD1D1D2)),
            Map.entry("pidgeot-mega", stone("Pidgeotite", 0xFFAF879D, 0xFFF7EF7C, 0xFFDD4173, 0xFFF79549)),
            Map.entry("pinsir-mega", stone("Pinsirite", 0xFFB04427, 0xFFA1706A, 0xFFF06F39, 0xFFF8DC39)),
            Map.entry("sableye-mega", stone("Sablenite", 0xFFAE4076, 0xFFE7285A, 0xFF795A9C, 0xFFFEDE7A)),
            Map.entry("salamence-mega", stone("Salamencite", 0xFF2B728B, 0xFFE2595F, 0xFF2A92BA, 0xFFC4C5C6)),
            Map.entry("sceptile-mega", stone("Sceptilite", 0xFF97734A, 0xFFDE6353, 0xFF274928, 0xFF74BC71)),
            Map.entry("scizor-mega", stone("Scizorite", 0xFF432629, 0xFFEB1F3B, 0xFF222D3B, 0xFFC7ECF1)),
            Map.entry("sharpedo-mega", stone("Sharpedonite", 0xFFCFC490, 0xFF3070A9, 0xFFFBDD81, 0xFFCDCCE5)),
            Map.entry("slowbro-mega", stone("Slowbronite", 0xFFDBC188, 0xFFF4808C, 0xFFF3EE89, 0xFFBACDBC)),
            Map.entry("steelix-mega", stone("Steelixite", 0xFFB3CADA, 0xFF3499E3, 0xFFCCECF4, 0xFF939AB3)),
            Map.entry("swampert-mega", stone("Swampertite", 0xFFAE6A6C, 0xFFEF6A5B, 0xFF49535B, 0xFF72B4DB)),
            Map.entry("tyranitar-mega", stone("Tyranitarite", 0xFF5E1F27, 0xFF1F1F26, 0xFFB3193E, 0xFFA0CA80)),
            Map.entry("venusaur-mega", stone("Venusaurite", 0xFFDB346F, 0xFF1C7B74, 0xFFF364A8, 0xFF60B8CC)),
            Map.entry("absol-mega-z", stone("Absolite Z", 0xFF111014, 0xFF2E6195, 0xFFDB015A, 0xFF00243C)),
            Map.entry("barbaracle-mega", stone("Barbaracite", 0xFFFCFEFE, 0xFFCED1DE, 0xFFF57757, 0xFF2B3236)),
            Map.entry("baxcalibur-mega", stone("Baxcalibrite", 0xFFBA3E41, 0xFF2F5E89, 0xFFB0A4E6, 0xFFC8EEEE)),
            Map.entry("chandelure-mega", stone("Chandelurite", 0xFFD7B179, 0xFF302F30, 0xFFDDEEFA, 0xFF7447F9)),
            Map.entry("chesnaught-mega", stone("Chesnaughtite", 0xFFD9B638, 0xFF467019, 0xFF87112C, 0xFFE9EADB)),
            Map.entry("chimecho-mega", stone("Chimechite", 0xFF367799, 0xFFE54262, 0xFFBADDE9, 0xFFF4DB70)),
            Map.entry("clefable-mega", stone("Clefablite", 0xFFFFFEFE, 0xFFFBE1D5, 0xFFE45386, 0xFFF78B9F)),
            Map.entry("crabominable-mega", stone("Crabominite", 0xFFE7CE42, 0xFF4DC9EA, 0xFF585478, 0xFFFFFFFF)),
            Map.entry("darkrai-mega", stone("Darkranite", 0xFF000000, 0xFFEDEDED, 0xFFFF22DC, 0xFF313031)),
            Map.entry("delphox-mega", stone("Delphoxite", 0xFFFDFDF8, 0xFFE1523D, 0xFFFBE67B, 0xFF413146)),
            Map.entry("dragalge-mega", stone("Dragalgite", 0xFF6D7A4D, 0xFF603731, 0xFFC65065, 0xFF996BE0)),
            Map.entry("dragonite-mega", stone("Dragoninite", 0xFFE256C3, 0xFFF7F1D2, 0xFF2CA796, 0xFFEEBB6C)),
            Map.entry("drampa-mega", stone("Drampanite", 0xFFE3ECDD, 0xFF3C4551, 0xFF1C916D, 0xFF67758E)),
            Map.entry("eelektross-mega", stone("Eelektrossite", 0xFFDE726D, 0xFF1F5D74, 0xFFF4DF64, 0xFFFEFEFE)),
            Map.entry("emboar-mega", stone("Emboarite", 0xFFD2794A, 0xFF454545, 0xFFE6C550, 0xFFDC5756)),
            Map.entry("excadrill-mega", stone("Excadrite", 0xFFA25457, 0xFF5B4E4A, 0xFFF9624C, 0xFFBABDBD)),
            Map.entry("falinks-mega", stone("Falinksite", 0xFF02A3FF, 0xFFE00200, 0xFF2D2927, 0xFFFFD302)),
            Map.entry("feraligatr-mega", stone("Feraligite", 0xFF4C7188, 0xFFDB3B5C, 0xFFFBE1A5, 0xFF68BCE8)),
            Map.entry("floette-eternal-mega", stone("Floettite", 0xFFFFFFFF, 0xFFDA0F3D, 0xFF3E5BA9, 0xFF383B3B)),
            Map.entry("froslass-mega", stone("Froslassite", 0xFFDC7164, 0xFFFDFDFD, 0xFF8677DC, 0xFF8BC5E7)),
            Map.entry("garchomp-mega-z", stone("Garchompite Z", 0xFFFDCE5B, 0xFFE73E0B, 0xFFFFFFFF, 0xFF484461)),
            Map.entry("glimmora-mega", stone("Glimmoranite", 0xFF6E76DC, 0xFF4AD3C7, 0xFF1B368F, 0xFF2172EA)),
            Map.entry("golisopod-mega", stone("Golisopite", 0xFFE7DE70, 0xFF873D99, 0xFF000000, 0xFF737A80)),
            Map.entry("golurk-mega", stone("Golurkite", 0xFF007097, 0xFFEFD484, 0xFFA05BAC, 0xFF7CA8B0)),
            Map.entry("greninja-mega", stone("Greninjite", 0xFFF4F1B5, 0xFF201F1F, 0xFFDE6E82, 0xFF68ACD6)),
            Map.entry("hawlucha-mega", stone("Hawluchanite", 0xFF363634, 0xFF66B99A, 0xFFE7C252, 0xFFBF4131)),
            Map.entry("heatran-mega", stone("Heatranite", 0xFFFFE200, 0xFFC9C9C9, 0xFFFFA801, 0xFFCF2C0E)),
            Map.entry("lucario-mega-z", stone("Lucarionite Z", 0xFF5B5F64, 0xFF409CAA, 0xFFAEBDC2, 0xFFE3EB8A)),
            Map.entry("magearna-mega", stone("Magearnite", 0xFFFFFFFF, 0xFFF9E27F, 0xFFFF98ED, 0xFFA91B2D)),
            Map.entry("malamar-mega", stone("Malamarite", 0xFFEFE77F, 0xFFCD468A, 0xFF58EBF5, 0xFF787AA4)),
            Map.entry("meganium-mega", stone("Meganiumite", 0xFFFFFFFF, 0xFFE14D52, 0xFFE97EA7, 0xFFC5FF9C)),
            Map.entry("meowstic-mega", stone("Meowsticite", 0xFF7DCBDC, 0xFFFFFFFF, 0xFFFFCD00, 0xFF00528C)),
            Map.entry("pyroar-mega", stone("Pyroarite", 0xFF2689AB, 0xFFF4E465, 0xFF5D504F, 0xFFCF192D)),
            Map.entry("raichu-mega-x", stone("Raichunite X", 0xFFE40029, 0xFF393939, 0xFF8B4A2D, 0xFFFDDA00)),
            Map.entry("raichu-mega-y", stone("Raichunite Y", 0xFF020000, 0xFFFEDB00, 0xFF5B452B, 0xFFF2A900)),
            Map.entry("scolipede-mega", stone("Scolipite", 0xFF29E1B0, 0xFFAB599B, 0xFF474C4B, 0xFF897E90)),
            Map.entry("scovillain-mega", stone("Scovillainite", 0xFF292A28, 0xFF406E50, 0xFFDF4F32, 0xFF5EAA5D)),
            Map.entry("scrafty-mega", stone("Scraftinite", 0xFF5F5F62, 0xFFEC9E69, 0xFFDA5957, 0xFFEEECEE)),
            Map.entry("skarmory-mega", stone("Skarmorite", 0xFF5D6F90, 0xFF3C4E6F, 0xFFEA3D2C, 0xFFE5C13B)),
            Map.entry("staraptor-mega", stone("Staraptite", 0xFFFCAD04, 0xFFF96038, 0xFF574F48, 0xFFC7BEBD)),
            Map.entry("starmie-mega", stone("Starminite", 0xFF3B71C1, 0xFFF7DB39, 0xFFBF1636, 0xFF7D79D1)),
            Map.entry("tatsugiri-mega", stone("Tatsugirinite", 0xFFFF692B, 0xFFFFDE00, 0xFFFF497A, 0xFF00DE63)),
            Map.entry("victreebel-mega", stone("Victreebelite", 0xFFDC9AA4, 0xFF9AC45C, 0xFFF89669, 0xFFE6DD55)),
            Map.entry("zeraora-mega", stone("Zeraorite", 0xFF6F7176, 0xFFEEDB5A, 0xFF80C5E6, 0xFF363835)),
            Map.entry("zygarde-mega", stone("Zygardite", 0xFF346DC5, 0xFF8BE044, 0xFFD02228, 0xFF504A41)));

    private MegaStoneColours()
    {
    }

    private static Stone stone(final String name, final int outline, final int detail, final int secondary,
            final int primary)
    {
        return new Stone(name, outline, detail, secondary, primary);
    }

    /**
     * @return the canonical Pokemon Legends: Z-A name for the Mega Stone, or {@code null} when no stone is known
     */
    public static String getName(final PokedexEntry mega)
    {
        if (mega == null) return null;
        final Stone stone = STONES.get(mega.getTrimmedName());
        return stone == null ? null : stone.name();
    }

    /**
     * @return a new four-colour array for the Mega entry, or {@code null} when no canonical stone is known
     */
    public static int[] get(final PokedexEntry mega)
    {
        if (mega == null) return null;
        final Stone stone = STONES.get(mega.getTrimmedName());
        return stone == null ? null : stone.toArray();
    }
}
