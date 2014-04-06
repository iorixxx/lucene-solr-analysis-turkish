package org.apache.lucene.analysis.tr.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.util.CharArrayMap;

final class MapG {

    static final CharArrayMap<Integer> map =
    CharArrayMap.unmodifiableMap(
    new CharArrayMap<Integer>(PatternTableFactory.version, 752, PatternTableFactory.ignoreCase)
    {{
            put(" s iX", 1); put(" oraX", -2); put("loXi ", 3);
            put("itelX", 4); put("zilXi", 5); put("r oXr", -6);
            put("aroXu", -7); put("teXes", -8); put("Ig aX", -9);
            put("zdIX ", -10); put("i teX", 11); put("p leX", 12);
            put("b OXe", -13); put("boruX", -14); put("dayIX", -15);
            put("moraX", -16); put("saXik", -17); put("h OXe", -18);
            put("o taX", -19); put("e eX ", -20); put("h maX", 21);
            put("aediX", -22); put("C veX", 23); put("padoX", -24);
            put("r aXn", -25); put("idiXu", 26); put("raXbi", -27);
            put("Xaini", 28); put("diXis", 29); put("dedeX", -30);
            put("remiX", -31); put(" tuXu", -32); put("rbuXu", -33);
            put("ihaXa", 34); put("reXle", -35); put("imilX", 36);
            put("akbuX", -37); put("Xayan", -38); put("atsaX", -39);
            put("y OXe", -40); put("nuraX", -41); put("seXis", -42);
            put("eXeti", 43); put(" IcoX", -44); put("saXat", 45);
            put("tlukX", 46); put("z edX", 47); put("azbeX", -48);
            put("ndaXa", -49); put("gOrXe", 50); put("ereX ", 51);
            put("Ge aX", 52); put("beliX", 53); put("coXal", -54);
            put("aldiX", -55); put("attiX", -56); put("Xard ", -57);
            put("aXall", -58); put("niXir", -59); put("viraX", -60);
            put("istiX", 61); put("tanoX", 62); put("zaXra", 63);
            put("eolaX", -64); put("guluX", -65); put(" toXr", 66);
            put("baXal", 67); put(" e aX", 68); put("yeleX", 69);
            put("reneX", 70); put("maXal", -71); put("Xarde", -72);
            put("erlaX", -73); put(" asaX", -74); put("ediXe", -75);
            put("dIXil", -76); put("boXan", 77); put("olleX", -78);
            put(" triX", -79); put(" blaX", -80); put("t aXr", 81);
            put(" eXen", 82); put("aireX", -83); put("hi OX", 84);
            put("Xers ", -85); put(" raXi", -86); put("iSeXe", -87);
            put("az aX", 88); put("lu aX", 89); put("Xulas", -90);
            put("s aX ", -91); put(" i OX", 92); put("eXen5", 93);
            put(" aXar", 94); put("oXlar", -95); put(" liXd", -96);
            put("0 doX", 97); put("n SaX", -98); put("Xusal", -99);
            put(" etiX", 100); put("gereX", 101); put("ca aX", 102);
            put("k daX", 103); put(" eroX", 104); put("ikliX", 105);
            put("aXisl", 106); put("Gi OX", 107); put("Xute ", -108);
            put("Xelir", -109); put("Xlene", 110); put("irliX", 111);
            put("Xerce", -112); put("ifeX", -113); put("maaX", -114);
            put(" SuX", -115); put("OliX", -116); put("cagX", 117);
            put("naXl", -118); put("huXu", -119); put(" dXe", 120);
            put("esaX", -121); put("neX ", 122); put(" iIX", -123);
            put("ylUX", -124); put("laXn", -125); put("siXb", 126);
            put("IXit", -127); put("caXr", 128); put("OzUX", -129);
            put("Xasv", -130); put("SUlX", 131); put("toX ", 132);
            put("vedX", -133); put("ltaX", -134); put("naXs", -135);
            put("dbaX", -136); put("nouX", 137); put("imXa", 138);
            put("CilX", 139); put(" dIX", -140); put("teXd", -141);
            put("gcaX", -142); put("Xcag", -143); put("taIX", -144);
            put("rOXe", -145); put(" dXu", 146); put("oldX", -147);
            put("ysaX", 148); put("ovaX", -149); put(" cXr", 150);
            put("C iX", 151); put(" iXe", -152); put("i sX", 153);
            put("oneX", -154); put("kreX", -155); put("yaXr", -156);
            put("CamX", 157); put("bOXl", -158); put("badX", -159);
            put("flaX", -160); put("lulX", 161); put("sraX", -162);
            put("taXl", -163); put("leXl", -164); put("moX ", 165);
            put("SIlX", 166); put("Xva ", 167); put("lilX", 168);
            put("vaXa", -169); put("IbuX", -170); put("Xnac", -171);
            put("eXn ", -172); put("Xanf", -173); put("ebuX", -174);
            put("ioXa", -175); put("nbuX", -176); put(" rIX", -177);
            put("hCeX", -178); put("zuXd", -179); put("aXia", -180);
            put("kriX", -181); put("ofaX", -182); put("hoXa", -183);
            put("Xann", -184); put("aXaf", -185); put("Xges", -186);
            put("bruX", -187); put("Xlai", -188); put("vkiX", -189);
            put("uyuX", -190); put("biXe", -191); put("Xnie", -192);
            put("utIX", -193); put("duXa", -194); put("seeX", -195);
            put("aleX", -196); put("laiX", 197); put("Xlaj", -198);
            put("Xgoz", 199); put("taXu", -200); put(" IXa", -201);
            put("mleX", 202); put("lbaX", 203); put("kXi ", 204);
            put("kiXi", 205); put("braX", -206); put("uXuk", -207);
            put(" riX", -208); put("rtXu", 209); put(" uXa", -210);
            put("rleX", 211); put("lXla", 212); put("duXi", 213);
            put("Xamo", -214); put("zaiX", 215); put("Xlac", 216);
            put("Xado", -217); put("apoX", 218); put("zcaX", 219);
            put("rluX", 220); put("eyoX", 221); put("baXr", 222);
            put("Xel ", -223); put("sliX", 224); put("zliX", 225);
            put("Xame", -226); put("icaX", -227); put("nliX", 228);
            put(" veX", -229); put("aXe ", -230); put("mliX", 231);
            put("Xerf", -232); put("alIX", 233); put("doXu", 234);
            put("tIXi", 235); put("oXf", -236); put("fUX", -237);
            put("wIX", -238); put("klX", 239); put("fOX", -240);
            put("uXz", -241); put("rdX", 242); put("sXl", 243);
            put("Xdy", -244); put("GGX", 245); put("ocX", 246);
            put("zlX", 247); put("uIX", -248); put("Xrk", 249);
            put("Xfo", -250); put("bIX", -251); put("Xce", 252);
            put("iGX", 253); put("IiX", 254); put("prX", 255);
            put("uXy", -256); put(" cX", -257); put("ucX", 258);
            put("Xea", -259); put("Xp ", -260); put("Xve", 261);
            put("aGX", -262); put("Xak", -263); put("Xei", -264);
            put("weX", -265); put("UXi", -266); put("mXy", 267);
            put("Xml", -268); put("rgX", 269); put("odX", -270);
            put("Xsy", -271); put("Xgy", -272); put("zeX", -273);
            put("heX", -274); put("Xba", 275); put("UXa", -276);
            put("yIX", 277); put("Xfl", -278); put("Xap", -279);
            put("gOX", 280); put("gaX", -281); put(" tX", -282);
            put("Xt ", -283); put("IoX", 284); put("Xav", -285);
            put("euX", -286); put("soX", 287); put("OXr", 288);
            put("0X",  289); put("wX", -290); put("Xq", -291);
            put("sX", -292); put("nX", -293); put("rX", -294);
            put("lI diyaloXun ", -295); put("mla diyaloX", -296);
            put("  fotoXrafe", -297); put("0 da fotoX", -298);
            put("ik OXeler ", -299); put("i OXeleri ", -300);
            put("s elekdaX", -301); put("k liXini ", 302);
            put("ve yaXin", -303); put("una aXit", 304);
            put("a bu OXe", -305); put("g civaoX", -306); put("toXrafik", -307);
            put("r buXu ", 308); put("ir doXm", -309); put("d baXir", -310);
            put("aik liX", 311); put("aXusta ", -312); put(" eleXe ", 313);
            put("tiracaX", -314); put("ikoloXl", -315); put("ngeliX", -316);
            put("lo aXa", -317); put("Xdatia", -318); put("ar moX", -319);
            put("a OXes", -320); put(" 8 liX", 321); put(" yaXil", -322);
            put(" y doX", -323); put("r OXel", -324); put("gorduX", -325);
            put("restiX", -326); put("IXaray", -327); put(" buXu ", -328);
            put("beSeX", -329); put("pfluX", -330); put("Xdela", -331);
            put("aXaz ", -332); put("capaX", -333); put("naXan", -334);
            put("Xunus", 335); put(" naXr", 336); put("iXre ", 337);
            put(" Xini", 338); put("C S X", 339); put(" bliX", -340);
            put("dIXan", -341); put("t OXe", -342); put("luXus", -343);
            put("etlaX", -344); put("kaXir", -345); put("OndeX", -346);
            put("sadiX", -347); put("aptiX", -348); put("laXil", -349);
            put("Xasio", -350); put("meXip", -351); put("5 liX", 352);
            put("iXlik", 353); put("solaX", -354); put(" aXil", 355);
            put("t oXa", 356); put("nz aX", -357); put("oXlas", -358);
            put(" leXi", -359); put("Ir aX", 360); put("arliX", -361);
            put("praX ", -362); put("saXar", -363); put("inaXa", -364);
            put("7 liX", 365); put("sa aX", 366); put("en OX", 367);
            put("OrneX", 368); put("a daX", 369); put("osiX", -370);
            put("ktaX", -371); put("Xnig", 372); put("graX", -373);
            put("niXb", 374); put("beaX", -375); put("ndXu", 376);
            put("udXu", 377); put("smiX", -378); put("yelX", 379);
            put("roXa", -380); put("maXm", -381); put("doXn", 382);
            put("cilX", 383); put("claX", -384); put(" faX", -385);
            put("naXr", -386); put("Xse ", 387); put(" uXl", -388);
            put("Xach", -389); put("sceX", -390); put("noXa", -391);
            put("Xuti", -392); put("aXgu", 393); put("aiXe", -394);
            put("eXla", -395); put("Xzan", -396); put("oXda", -397);
            put("saXl", 398); put("Xlin", -399); put("ioXr", -400);
            put("raXu", -401); put("eluX", -402); put("eXra", -403);
            put(" toX", -404); put("ldiX", 405); put("baXi", 406);
            put("ttiX", 407); put("Xelm", -408); put("Xast", -409);
            put("IaXi", -410); put("lduX", 411); put("gIX", -412);
            put("oGX", 413); put("oXb", -414); put(" gX", 415);
            put("mcX", -416); put("juX", -417); put("Xiv", -418);
            put("Xga", -419); put("nUX", -420); put("eXg", -421);
            put("Xno", -422); put("sIX", 423); put("iXa", -424);
            put("oXy", -425); put("CoX", 426); put("Xto", -427);
            put("pX", -428); put("an OXeler ", -429); put("li OXes", -430);
            put(" leXeni", 431); put("iliXis", -432); put(" maXas", -433);
            put(" uXula", -434); put("oyacIX", -435); put("Xrafyo", 436);
            put("kanIlX", 437); put("leXenl", 438); put("ye aXi", 439);
            put("Xrafk", -440); put("Xmayp", -441); put("straX", -442);
            put(" laXa", -443); put("maXad", -444); put("malaX", -445);
            put("Xlise", -446); put("baliX", 447); put("eXida", -448);
            put("baXaz", 449); put("apliX", -450); put("moXal", 451);
            put("taraX", 452); put("e taX", -453); put(" oXan", -454);
            put("litoX", -455); put("bu aX", 456); put("oXman", -457);
            put("bsaX", -458); put(" pOX", -459); put("bOXe", -460);
            put(" giX", -461); put(" IX ", -462); put("liXm", 463);
            put(" mIX", -464); put("otaX", 465); put("kilX", 466);
            put("yeX ", 467); put("rilX", 468); put("Xing", -469);
            put("kruX", -470); put("druX", -471); put(" zoX", -472);
            put("OceX", 473); put("taXi", 474); put("Xanu", 475);
            put("driX", -476); put("roXr", -477); put("lOX", -478);
            put("Xaw", -479); put("IXe", -480); put("Xf ", -481);
            put("iXc", 482); put("atX", -483); put("Xd ", -484);
            put("wiX", -485); put("feX", 486); put("hiX", -487);
            put("eXa", -488); put("bX", -489); put("kX", -490);
            put(" X", -491); put("Xalass", -492); put("ileXe ", 493);
            put("baXan ", 494); put("unaXa", -495); put(" aXik", -496);
            put("beXel", -497); put(" raXa", -498); put(" oriX", -499);
            put("aXrot", 500); put("ediX ", 501); put(" diX ", 502);
            put("maXda", -503); put("kuruX", -504); put("eoloX", -505);
            put("soluX", 506); put("ir aX", 507); put("Xarli", -508);
            put("anuX", -509); put("deXu", -510); put("oXle", -511);
            put("eXm ", -512); put("truX", -513); put("bUXe", -514);
            put("niaX", -515); put("iXes", -516); put(" vaX", -517);
            put("sIlX", 518); put(" ziX", -519); put("ktiX", 520);
            put("fraX", -521); put("rlX", 522); put("ntX", -523);
            put("Xmi", 524); put("eXo", -525); put("Xy ", -526);
            put("aXandan ", 527); put("poXrafi", -528); put("im beX", 529);
            put("meneX", -530); put(" tiXe", -531); put(" iXde", 532);
            put("mireX", 533); put("iXida", -534); put("matoX", -535);
            put("baXa ", 536); put(" a aX", 537); put("oXli", -538);
            put("iXib", -539); put("viX ", 540); put("taXn", -541);
            put("Xdeb", -542); put(" hoX", -543); put("siXn", -544);
            put(" ruX", -545); put("rUlX", 546); put("elaX", -547);
            put("proX", -548); put("Xlo", -549); put("er OXe", -550);
            put("boXus ", -551); put("rdiX ", 552); put("leXer", -553);
            put("oXano", 554); put(" muXa", -555); put("liXn", 556);
            put(" eX ", 557); put("yoXa", -558); put("Xrap", -559);
            put(" CaX", 560); put("waX", -561); put("subuX", -563);
            put(" maXi", -564); put("cIlXi", 565); put("neXer", -566);
            put("SeXe ", 567); put(" beXi", -568); put(" oXd", -569);
            put(" iXi", -570); put("uXle", -571); put("Xiad", -572);
            put("opaX", -573); put("Xb ", -574); put("eiX", -575);
            put(" maXa ", -576); put("Xinat", -577); put("oXlan", 578);
            put(" eXt", 579); put("Xask", -580); put("luXg", -581);
            put("paXu", -582); put("coiX", -583); put("uXar", -584);
            put("afX", -585); put("irbaX", -586); put("oXart", -587);
            put("moXa", -588); put("ruXg", -589); put("nlX", 590);
            put("Xgs", -591); put("aXasi ", 592); put("Xment", -593);
            put(" buXs", -594); put("bUXu", -595); put(" paX", -596);
            put("taiX", -597); put("gzaX", -598); put("draX", -599);
            put("mX", -600); put("lIXil", -601); put(" IXl", -602);
            put("IXaz", -603); put(" biX", -604); put("Xro", -605);
            put("Xland ", -606); put("aXun", -607); put("aXuc", -608);
            put("aXle", -609); put("guX", -610); put("Xai", -611);
            put("Xui", -612); put("leXen ", 613); put(" naXa", -614);
            put("bliXd", 615); put("Xmati", -616); put(" IXn", -617);
            put("maX ", -618); put("oXi", -619); put(" briX", -620);
            put("Xlia", -621); put("OXet", -622); put("luiX", -623);
            put("efiX", 624); put("OpeX", 625); put(" yiX", 626);
            put("Xmo", -627); put(" OXer ", -628); put("oXmala", -629);
            put("gulaX", -630); put("koXr", -631); put("Xass", -632);
            put("Xas ", -633); put("mUX", -634); put("ieX", -635);
            put("oXn", -636); put(" naXi", -637); put(" reX", -638);
            put(" zaX", -639); put("Xue", -640); put("uXi", -641);
            put("m aX ", -642); put("Xanis", -643); put("maXn", -644);
            put("Xdi", 645); put("seXe", 646); put("uXan", -647);
            put(" saXan ", -648); put("aXil ", -649); put(" diXi", -650);
            put("Xgi", -651); put("taX ", -652); put("goX", -653);
            put("CX", -654); put("Xie ", -655); put("Xs ", -656);
            put("Xah", -657); put("SX", -658); put("bliXl", 659);
            put(" CaXa", 660); put("aXac", 661); put("iXf", 662);
            put("moXr", -663); put("voX", -664); put("oXre", -665);
            put("toXa", -666); put("Xat", -668); put("doXr", 669);
            put("iXel", -670); put(" moXo", 671); put("iXil ", -672);
            put("aXip ", -673); put("Xua", -674); put("iXem", -675);
            put("Xay ", -676); put("reXe ", 677); put("oX ", -678);
            put("Xide", -679); put("yun eX", 680); put(" aXit ", -681);
            put("dar tuXla ", -682); put("aXne", -683); put("auX", -684);
            put("oXa ", 685); put("aXg", -686); put("rkeX", 687);
            put(" tUX", -688); put("noXr", -689); put(" iXr", 690);
            put(" saXo", 691); put("iyoX", -692); put("gX", -693);
            put("eaXa", -694); put("ouX", -695); put("eoXr", -696);
            put("aXul", -697); put(" eXr", 698); put("Xic", -699);
            put("neXe ", 700); put("iXen", -701); put("CeXe", 702);
            put("deX", 703); put("aXre", -704); put("uXe", -705);
            put("sleX", 706); put("aXra", -707); put("Xano", -708);
            put("oXe", -709); put("bliX ", 710); put(" baXa", -711);
            put("CiX", 712); put("aXazi", -713); put("IXu", -714);
            put("teXe", 715); put("yeXe", 716); put(" vuX", -717);
            put("meXe ", 718); put("niXd", 719); put("eXm", 720);
            put(" oXun", -721); put(" praX", -722); put("Xio", -723);
            put("eceX", 724); put("oXu", 725); put("aXe", -726);
            put("oXlu", 727); put("Xanda", -728); put("araX", -729);
            put("uXay", -730); put("Xne", 731); put("Xh", -732);
            put("haX", -733); put("beXe", 734); put("eXl", 735);
            put("Xo", -736); put("loX", -737); put(" liX", -738);
            put("zX", -739); put("vX", -740); put("IX", 741);
            put("yX", -742); put("oXram", -743); put("iXe", 744);
            put("eXer", 745); put(" buXun", -746); put("UX", 747);
            put("OX", 750); put("lX", -751); put("oX", 752);
            put("uX", 753); put("aX", 754); put("Xi", 756); put("X", -757);

        }}
    );

    private MapG() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
