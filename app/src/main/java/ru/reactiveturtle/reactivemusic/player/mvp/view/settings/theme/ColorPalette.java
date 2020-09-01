package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import java.util.Arrays;

public class ColorPalette {
    /*
     * primaryDark|primary|primaryLight
     * RED
     * PINK
     * PURPLE
     * DEEP_PURPLE
     * INDIGO
     * BLUE
     * LIGHT_BLUE
     * CYAN
     * TEAL
     * GREEN
     * LIGHT_GREEN
     * LIME
     * YELLOW
     * AMBER
     * ORANGE
     * DEEP_ORANGE
     * BROWN
     * GREY
     * BLUE_GREY
     */

    private static final String[] NAMES = new String[]{
            "50", "100", "200",
            "300", "400", "500",
            "600", "700", "800",
            "900", "A100", "A200",
            "A400", "A700"
    };

    public static String[] getNames() {
        return Arrays.copyOf(NAMES, NAMES.length);
    }

    public static final String M_50 = ""
            + "ccb9bc|ffebee|ffffff|" //RED
            + "c9b2ba|fce4ec|ffffff|" //PINK
            + "c0b3c2|f3e5f5|ffffff|" //PURPLE
            + "bbb5c3|ede7f6|ffffff|" //DEEP_PURPLE
            + "b6b8c3|e8eaf6|ffffff|" //INDIGO
            + "b1bfca|e3f2fd|ffffff|" //BLUE
            + "afc2cb|e1f5fe|ffffff|" //LIGHT_BLUE
            + "aec4c7|e0f7fa|ffffff|" //CYAN
            + "aebfbe|e0f2f1|ffffff|" //TEAL
            + "b6c2b7|e8f5e9|ffffff|" //GREEN
            + "bec5b7|f1f8e9|ffffff|" //LIGHT_GREEN
            + "c6c8b5|f9fbe7|ffffff|" //LIME
            + "cccab5|fffde7|ffffff|" //YELLOW
            + "ccc5af|fff8e1|ffffff|" //AMBER
            + "ccc0ae|fff3e0|ffffff|" //ORANGE
            + "c8b7b5|fbe9e7|ffffff|" //DEEP_ORANGE
            + "bdb9b7|efebe9|ffffff|" //BROWN
            + "c7c7c7|fafafa|ffffff|" //GREY
            + "babdbe|eceff1|ffffff"; //BLUE_GREY

    public static final String M_100 = ""
            + "cb9ca1|ffcdd2|ffffff|" //RED
            + "c48b9f|f8bbd0|ffeeff|" //PINK
            + "af8eb5|e1bee7|fff1ff|" //PURPLE
            + "a094b7|d1c4e9|fff7ff|" //DEEP_PURPLE
            + "9499b7|c5cae9|f8fdff|" //INDIGO
            + "8aacc8|bbdefb|eeffff|" //BLUE
            + "82b3c9|b3e5fc|e6ffff|" //LIGHT_BLUE
            + "81b9bf|b2ebf2|e5ffff|" //CYAN
            + "82ada9|b2dfdb|e5ffff|" //TEAL
            + "97b498|c8e6c9|fbfffc|" //GREEN
            + "aabb97|dcedc8|fffffb|" //LIGHT_GREEN
            + "bdc192|f0f4c3|fffff6|" //LIME
            + "cbc693|fff9c4|fffff7|" //YELLOW
            + "cbba83|ffecb3|ffffe5|" //AMBER
            + "cbae82|ffe0b2|ffffe4|" //ORANGE
            + "cb9b8c|ffccbc|ffffee|" //DEEP_ORANGE
            + "a69b97|d7ccc8|fffffb|" //BROWN
            + "c2c2c2|f5f5f5|ffffff|" //GREY
            + "9ea7aa|cfd8dc|ffffff"; //BLUE_GREY

    public static final String M_200 = ""
            + "ba6b6c|ef9a9a|ffcccb|" //RED
            + "bf5f82|f48fb1|ffc1e3|" //PINK
            + "9c64a6|ce93d8|ffc4ff|" //PURPLE
            + "836fa9|b39ddb|e6ceff|" //DEEP_PURPLE
            + "6f79a8|9fa8da|d1d9ff|" //INDIGO
            + "5d99c6|90caf9|c3fdff|" //BLUE
            + "4ba3c7|81d4fa|b6ffff|" //LIGHT_BLUE
            + "4bacb8|80deea|b4ffff|" //CYAN
            + "4f9a94|80cbc4|b2fef7|" //TEAL
            + "75a478|a5d6a7|d7ffd9|" //GREEN
            + "94af76|c5e1a5|f8ffd7|" //LIGHT_GREEN
            + "b3bc6d|e6ee9c|ffffce|" //LIME
            + "cbc26d|fff59d|ffffcf|" //YELLOW
            + "caae53|ffe082|ffffb3|" //AMBER
            + "ca9b52|ffcc80|ffffb0|" //ORANGE
            + "c97b63|ffab91|ffddc1|" //DEEP_ORANGE
            + "8c7b75|bcaaa4|efdcd5|" //BROWN
            + "bcbcbc|eeeeee|ffffff|" //GREY
            + "808e95|b0bec5|e2f1f8"; //BLUE_GREY

    public static final String M_300 = ""
            + "af4448|e57373|ffa4a2|" //RED
            + "ba2d65|f06292|ff94c2|" //PINK
            + "883997|ba68c8|ee98fb|" //PURPLE
            + "65499c|9575cd|c7a4ff|" //DEEP_PURPLE
            + "49599a|7986cb|aab6fe|" //INDIGO
            + "2286c3|64b5f6|9be7ff|" //BLUE
            + "0093c4|4fc3f7|8bf6ff|" //LIGHT_BLUE
            + "009faf|4dd0e1|88ffff|" //CYAN
            + "00867d|4db6ac|82e9de|" //TEAL
            + "519657|81c784|b2fab4|" //GREEN
            + "7da453|aed581|e1ffb1|" //LIGHT_GREEN
            + "a8b545|dce775|ffffa6|" //LIME
            + "cabf45|fff176|ffffa8|" //YELLOW
            + "c8a415|ffd54f|ffff81|" //AMBER
            + "c88719|ffb74d|ffe97d|" //ORANGE
            + "c75b39|ff8a65|ffbb93|" //DEEP_ORANGE
            + "725b53|a1887f|d3b8ae|" //BROWN
            + "aeaeae|e0e0e0|ffffff|" //GREY
            + "62757f|90a4ae|c1d5e0"; //BLUE_GREY

    public static final String M_400 = ""
            + "b61827|ef5350|ff867c|" //RED
            + "b4004e|ec407a|ff77a9|" //PINK
            + "790e8b|ab47bc|df78ef|" //PURPLE
            + "4d2c91|7e57c2|b085f5|" //DEEP_PURPLE
            + "26418f|5c6bc0|8e99f3|" //INDIGO
            + "0077c2|42a5f5|80d6ff|" //BLUE
            + "0086c3|29b6f6|73e8ff|" //LIGHT_BLUE
            + "0095a8|26c6da|6ff9ff|" //CYAN
            + "00766c|26a69a|64d8cb|" //TEAL
            + "338a3e|66bb6a|98ee99|" //GREEN
            + "6b9b37|9ccc65|cfff95|" //LIGHT_GREEN
            + "a0af22|d4e157|ffff89|" //LIME
            + "c9bc1f|ffee58|ffff8b|" //YELLOW
            + "c79a00|ffca28|fffd61|" //AMBER
            + "c77800|ffa726|ffd95b|" //ORANGE
            + "c63f17|ff7043|ffa270|" //DEEP_ORANGE
            + "5f4339|8d6e63|be9c91|" //BROWN
            + "8d8d8d|bdbdbd|efefef|" //GREY
            + "4b636e|78909c|a7c0cd"; //BLUE_GREY

    public static final String M_500 = ""
            + "ba000d|f44336|ff7961|" //RED
            + "b0003a|e91e63|ff6090|" //PINK
            + "6a0080|9c27b0|d05ce3|" //PURPLE
            + "320b86|673ab7|9a67ea|" //DEEP_PURPLE
            + "002984|3f51b5|757de8|" //INDIGO
            + "0069c0|2196f3|6ec6ff|" //BLUE
            + "007ac1|03a9f4|67daff|" //LIGHT_BLUE
            + "008ba3|00bcd4|62efff|" //CYAN
            + "00675b|009688|52c7b8|" //TEAL
            + "087f23|4caf50|80e27e|" //GREEN
            + "5a9216|8bc34a|bef67a|" //LIGHT_GREEN
            + "99aa00|cddc39|ffff6e|" //LIME
            + "c8b900|ffeb3b|ffff72|" //YELLOW
            + "c79100|ffc107|fff350|" //AMBER
            + "c66900|ff9800|ffc947|" //ORANGE
            + "c41c00|ff5722|ff8a50|" //DEEP_ORANGE
            + "4b2c20|795548|a98274|" //BROWN
            + "707070|9e9e9e|cfcfcf|" //GREY
            + "34515e|607d8b|8eacbb"; //BLUE_GREY

    public static final String M_600 = ""
            + "ab000d|e53935|ff6f60|" //RED
            + "a00037|d81b60|ff5c8d|" //PINK
            + "5c007a|8e24aa|c158dc|" //PURPLE
            + "280680|5e35b1|9162e4|" //DEEP_PURPLE
            + "00227b|3949ab|6f74dd|" //INDIGO
            + "005cb2|1e88e5|6ab7ff|" //BLUE
            + "006db3|039be5|63ccff|" //LIGHT_BLUE
            + "007c91|00acc1|5ddef4|" //CYAN
            + "005b4f|00897b|4ebaaa|" //TEAL
            + "00701a|43a047|76d275|" //GREEN
            + "4b830d|7cb342|aee571|" //LIGHT_GREEN
            + "8c9900|c0ca33|f5fd67|" //LIME
            + "c6a700|fdd835|ffff6b|" //YELLOW
            + "c68400|ffb300|ffe54c|" //AMBER
            + "c25e00|fb8c00|ffbd45|" //ORANGE
            + "b91400|f4511e|ff844c|" //DEEP_ORANGE
            + "40241a|6d4c41|9c786c|" //BROWN
            + "494949|757575|a4a4a4|" //GREY
            + "29434e|546e7a|819ca9"; //BLUE_GREY

    public static final String M_700 = ""
            + "9a0007|d32f2f|ff6659|" //RED
            + "8c0032|c2185b|fa5788|" //PINK
            + "4a0072|7b1fa2|ae52d4|" //PURPLE
            + "140078|512da8|8559da|" //DEEP_PURPLE
            + "001970|303f9f|666ad1|" //INDIGO
            + "004ba0|1976d2|63a4ff|" //BLUE
            + "005b9f|0288d1|5eb8ff|" //LIGHT_BLUE
            + "006978|0097a7|56c8d8|" //CYAN
            + "004c40|00796b|48a999|" //TEAL
            + "00600f|388e3c|6abf69|" //GREEN
            + "387002|689f38|99d066|" //LIGHT_GREEN
            + "7c8500|afb42b|e4e65e|" //LIME
            + "c49000|fbc02d|fff263|" //YELLOW
            + "c67100|ffa000|ffd149|" //AMBER
            + "bb4d00|f57c00|ffad42|" //ORANGE
            + "ac0800|e64a19|ff7d47|" //DEEP_ORANGE
            + "321911|5d4037|8b6b61|" //BROWN
            + "373737|616161|8e8e8e|" //GREY
            + "1c313a|455a64|718792"; //BLUE_GREY

    public static final String M_800 = ""
            + "8e0000|c62828|ff5f52|" //RED
            + "78002e|ad1457|e35183|" //PINK
            + "38006b|6a1b9a|9c4dcc|" //PURPLE
            + "000070|4527a0|7953d2|" //DEEP_PURPLE
            + "001064|283593|5f5fc4|" //INDIGO
            + "003c8f|1565c0|5e92f3|" //BLUE
            + "004c8c|0277bd|58a5f0|" //LIGHT_BLUE
            + "005662|00838f|4fb3bf|" //CYAN
            + "003d33|00695c|439889|" //TEAL
            + "005005|2e7d32|60ad5e|" //GREEN
            + "255d00|558b2f|85bb5c|" //LIGHT_GREEN
            + "6c6f00|9e9d24|d2ce56|" //LIME
            + "c17900|f9a825|ffd95a|" //YELLOW
            + "c56000|ff8f00|ffc046|" //AMBER
            + "b53d00|ef6c00|ff9d3f|" //ORANGE
            + "9f0000|d84315|ff7543|" //DEEP_ORANGE
            + "260e04|4e342e|7b5e57|" //BROWN
            + "1b1b1b|424242|6d6d6d|" //GREY
            + "102027|37474f|62727b"; //BLUE_GREY

    public static final String M_900 = ""
            + "7f0000|b71c1c|f05545|" //RED
            + "560027|880e4f|bc477b|" //PINK
            + "12005e|4a148c|7c43bd|" //PURPLE
            + "000063|311b92|6746c3|" //DEEP_PURPLE
            + "000051|1a237e|534bae|" //INDIGO
            + "002171|0d47a1|5472d3|" //BLUE
            + "002f6c|01579b|4f83cc|" //LIGHT_BLUE
            + "00363a|006064|428e92|" //CYAN
            + "00251a|004d40|39796b|" //TEAL
            + "003300|1b5e20|4c8c4a|" //GREEN
            + "003d00|33691e|629749|" //LIGHT_GREEN
            + "524c00|827717|b4a647|" //LIME
            + "bc5100|f57f17|ffb04c|" //YELLOW
            + "c43e00|ff6f00|ffa040|" //AMBER
            + "ac1900|e65100|ff833a|" //ORANGE
            + "870000|bf360c|f9683a|" //DEEP_ORANGE
            + "1b0000|3e2723|6a4f4b|" //BROWN
            + "000000|212121|484848|" //GREY
            + "000a12|263238|4f5b62"; //BLUE_GREY

    public static final String M_A100 = ""
            + "c85a54|ff8a80|ffbcaf|" //RED
            + "c94f7c|ff80ab|ffb2dd|" //PINK
            + "b64fc8|ea80fc|ffb2ff|" //PURPLE
            + "805acb|b388ff|e7b9ff|" //DEEP_PURPLE
            + "5870cb|8c9eff|c0cfff|" //INDIGO
            + "4d82cb|82b1ff|b6e3ff|" //BLUE
            + "49a7cc|80d8ff|b5ffff|" //LIGHT_BLUE
            + "4bcbcc|84ffff|baffff|" //CYAN
            + "75ccb9|a7ffeb|dbffff|" //TEAL
            + "88c399|b9f6ca|ecfffd|" //GREEN
            + "99cc60|ccff90|ffffc2|" //LIGHT_GREEN
            + "bfcc50|f4ff81|ffffb3|" //LIME
            + "cacc5d|ffff8d|ffffbf|" //YELLOW
            + "cab350|ffe57f|ffffb0|" //AMBER
            + "caa052|ffd180|ffffb1|" //ORANGE
            + "c96f53|ff9e80|ffd0b0"; //DEEP_ORANGE

    public static final String M_A200 = ""
            + "c50e29|ff5252|ff867f|" //RED
            + "c60055|ff4081|ff79b0|" //PINK
            + "aa00c7|e040fb|ff79ff|" //PURPLE
            + "3f1dcb|7c4dff|b47cff|" //DEEP_PURPLE
            + "0043ca|536dfe|8f9bff|" //INDIGO
            + "005ecb|448aff|83b9ff|" //BLUE
            + "0094cc|40c4ff|82f7ff|" //LIGHT_BLUE
            + "00cbcc|18ffff|76ffff|" //CYAN
            + "14cba8|64ffda|9effff|" //TEAL
            + "2bbd7e|69f0ae|9fffe0|" //GREEN
            + "7ecb20|b2ff59|e7ff8c|" //LIGHT_GREEN
            + "b8cc00|eeff41|ffff78|" //LIME
            + "c7cc00|ffff00|ffff5a|" //YELLOW
            + "c8a600|ffd740|ffff74|" //AMBER
            + "c77c02|ffab40|ffdd71|" //ORANGE
            + "c53d13|ff6e40|ffa06d"; //DEEP_ORANGE

    public static final String M_A400 = ""
            + "c4001d|ff1744|ff616f|" //RED
            + "bb002f|f50057|ff5983|" //PINK
            + "9e00c5|d500f9|ff5bff|" //PURPLE
            + "0100ca|651fff|a255ff|" //DEEP_PURPLE
            + "0031ca|3d5afe|8187ff|" //INDIGO
            + "004ecb|2979ff|75a7ff|" //BLUE
            + "0081cb|00b0ff|69e2ff|" //LIGHT_BLUE
            + "00b2cc|00e5ff|6effff|" //CYAN
            + "00b686|1de9b6|6effe8|" //TEAL
            + "00b248|00e676|66ffa6|" //GREEN
            + "32cb00|76ff03|b0ff57|" //LIGHT_GREEN
            + "90cc00|c6ff00|fdff58|" //LIME
            + "c7b800|ffea00|ffff56|" //YELLOW
            + "c79400|ffc400|fff64f|" //AMBER
            + "c56200|ff9100|ffc246|" //ORANGE
            + "c30000|ff3d00|ff7539"; //DEEP_ORANGE

    public static final String M_A700 = ""
            + "9b0000|d50000|ff5131|" //RED
            + "8e0038|c51162|fd558f|" //PINK
            + "7200ca|aa00ff|e254ff|" //PURPLE
            + "0a00b6|6200ea|9d46ff|" //DEEP_PURPLE
            + "0026ca|304ffe|7a7cff|" //INDIGO
            + "0039cb|2962ff|768fff|" //BLUE
            + "0064b7|0091ea|64c1ff|" //LIGHT_BLUE
            + "0088a3|00b8d4|62ebff|" //CYAN
            + "008e76|00bfa5|5df2d6|" //TEAL
            + "009624|00c853|5efc82|" //GREEN
            + "1faa00|64dd17|9cff57|" //LIGHT_GREEN
            + "79b700|aeea00|e4ff54|" //LIME
            + "c7a500|ffd600|ffff52|" //YELLOW
            + "c67c00|ffab00|ffdd4b|" //AMBER
            + "c43c00|ff6d00|ff9e40|" //ORANGE
            + "a30000|dd2c00|ff6434"; //DEEP_ORANGE
}
