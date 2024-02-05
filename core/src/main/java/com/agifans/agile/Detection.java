package com.agifans.agile;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.agifans.agile.agilib.Game;
import com.agifans.agile.util.StringUtils;

/**
 * The Detection class handles detection of AGI games, demos and fan made games.
 */
public class Detection {

    /**
     * The short ID of the game, as known by the AGILE interpreter.
     */
    public String gameId;

    /**
     * The displayable name of the game.
     */
    public String gameName = "Unrecognised game";
    
    /**
     * If the game requires the AGI mouse hack.
     */
    public boolean hasAGIMouse;
    
    /**
     * If the game requires the AGIPAL hack.
     */
    public boolean hasAGIPal;
    
    /**
     * If the game requires the AGI256 hack.
     */
    public boolean hasAGI256;
    
    /**
     * Constructor for Detection.
     * 
     * @param game
     */
    public Detection(Game game) {
        try {
            String dirPrefix = (game.v3GameSig != null? game.v3GameSig.toLowerCase() : "log");
            
            // Calculate MD5 hash of the game.
            byte[] data = game.gameFilesMap.get(dirPrefix + "dir");
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            String md5HashString = StringUtils.padLeftZeros(new BigInteger(1, hash).toString(16), 32);
            String detectedGameId = null;
            String flags = null;
            
            // Compare with known MD5 hash values for AGI games and demos.
            for (int i = 0; i < gameDefinitions.length; i++) {
                if (gameDefinitions[i][3].equals(md5HashString)) {
                    detectedGameId = gameDefinitions[i][1];
                    gameName = gameDefinitions[i][2];
                    flags = gameDefinitions[i][0];
                    hasAGIMouse = flags.contains("M");
                    hasAGIPal = flags.contains("P") || hasAGIPalFiles(game);
                    hasAGI256 = flags.contains("V");
                    break;
                }
            }
            
            // Work out the best value to use for the game ID.
            if (detectedGameId != null) {
                gameId = detectedGameId;
            }
            else if (game.gameId != null) {
                gameId = game.gameId;
            }
            else if (game.v3GameSig != null) {
                gameId = game.v3GameSig;
            }
            else {
                // Use first 6 characters of the MD5 hash. Normally we'd hope that this
                // fallback isn't used, as it is much better that we have a well known
                // value for the game ID.
                gameId = md5HashString.substring(0, 6).toUpperCase();
            }
            
            System.out.print("MD5 Hash: " + md5HashString);
            System.out.print(", Game Name: " + gameName);
            System.out.print(", Game ID (detected): " + gameId);
            System.out.print(", Game ID (in game): " + game.gameId);
            System.out.println(", V3 Sig: " + game.v3GameSig);
        }
        catch (Exception e) {
            // Failure in game detection code. Continue with the default unrecognised game values.
            e.printStackTrace();
        }
    }
    
    /**
     * Checks the Game's gameFilesMap to determine if there are AGI PAL files. Returns
     * true if there are; otherwise false.
     * 
     * @param game The Game to look for PAL files within.
     * 
     * @return true if the Game has AGI PAL files; otherwise false.
     */
    private boolean hasAGIPalFiles(Game game) {
        if ((game != null) && (game.gameFilesMap != null)) {
            for (String fileName : game.gameFilesMap.keySet()) {
                if (fileName.toLowerCase().matches("^pal[.]10[0-9]$")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * MD5 hash values for known games and demos.
     * 
     * The Game ID has been set to align with the original AGI games in the cases where
     * the game sets the game ID within its LOGIC scripts. All original AGI games do that,
     * with the except of MH1, so we have set that to follow the convention used by MH2.
     * The game IDs have also been configured in uppercase, which is the Sierra AGI 
     * conversation, and a max length of 6 has been chosen (despite AGI's max being 7) 
     * due to the saved game file needing to add "SG" as a suffix.
     */
    private static String[][] gameDefinitions = {
        {"----", "DEMO", "AGI Demo 1 (1987-05-20)", "9c4a5b09cc3564bc48b4766e679ea332"},
        {"----", "DEMO2", "AGI Demo 2 (1987-11-24 3.5\")", "e8ebeb0bbe978172fe166f91f51598c7"},
        {"----", "DEMO2", "AGI Demo 2 (1987-11-24 [version 1] 5.25\")", "852ac303a374df62571642ca1e2d1f0a"},
        {"----", "DEMO2", "AGI Demo 2 (1987-11-25 [version 2] 5.25\")", "1503f02086ea9f388e7e041c039eaa69"},
        {"----", "DEMO2", "AGI Demo 2 (Tandy)", "94eca021fe7da8f8572c2edcc631bbc6"},
        {"----", "DEMOKS", "AGI Demo Kings Quest III and Space Quest I", "502e6bf96827b6c4d3e67c9cdccd1033"},
        {"----", "BC", "The Black Cauldron (2.00 1987-06-14)", "7f598d4712319b09d7bd5b3be10a2e4a"},
        {"----", "DP", "Donald Duck's Playground (1.0A 1986-08-08)", "64388812e25dbd75f7af1103bc348596"},
        {"----", "DP", "Donald Duck's Playground (1.0C 1986-06-09)", "550971d196f65190a5c760d2479406ef"},
        {"----", "DP", "Donald Duck's Playground (1.50 1987-06-22)", "268074cc8cb75aa2227c4398886d7acd"},
        {"----", "KQ1", "King's Quest I (2.0F 1987-05-05 5.25\"/3.5\")", "10ad66e2ecbd66951534a50aedcd0128"},
        {"----", "KQ2", "King's Quest II (2.1 1987-04-10)", "759e39f891a0e1d86dd29d7de485c6ac"},
        {"----", "KQ2", "King's Quest II (2.2 1987-05-07 5.25\"/3.5\")", "b944c4ff18fb8867362dc21cc688a283"},
        {"----", "KQ3", "King's Quest III (1.01 1986-11-08)", "9c2b34e7ffaa89c8e2ecfeb3695d444b"},
        {"----", "KQ3", "King's Quest III (2.00 1987-05-25 5.25\")", "18aad8f7acaaff760720c5c6885b6bab"},
        {"----", "KQ3", "King's Quest III (2.00 1987-05-25 5.25\")", "b46dc63d6272fb6ed24a004ad580a033"},
        {"----", "KQ3", "King's Quest III (2.14 1988-03-15 3.5\")", "d3d17b77b3b3cd13246749231d9473cd"},
        {"----", "LLLLL", "Leisure Suit Larry (1.00 1987-06-01 5.25\"/3.5\")", "1fe764e66857e7f305a5f03ca3f4971d"},
        {"----", "MG", "Mixed Up Mother Goose (1987-11-10)", "e524655abf9b96a3b179ffcd1d0f79af"},
        {"----", "PQ", "Police Quest (2.0E 1987-11-17)", "2fd992a92df6ab0461d5a2cd83c72139"},
        {"----", "PQ", "Police Quest (2.0A 1987-10-23)", "b9dbb305092851da5e34d6a9f00240b1"},
        {"----", "PQ", "Police Quest (2.0G 1987-12-03 5.25\"/ST)", "231f3e28170d6e982fc0ced4c98c5c1c"},
        {"----", "PQ", "Police Quest (2.0G 1987-12-03)", "d194e5d88363095f55d5096b8e32fbbb"},
        {"----", "SQ", "Space Quest I (1.1A 1986-11-13)", "8d8c20ab9f4b6e4817698637174a1cb6"},
        {"----", "SQ", "Space Quest I (1.1A 720kb)", "0a92b1be7daf3bb98caad3f849868aeb"},
        {"----", "SQ", "Space Quest I (1.0X 1986-09-24)", "af93941b6c51460790a9efa0e8cb7122"},
        {"----", "SQ", "Space Quest I (2.2 1987-05-07 5.25\"/3.5\")", "5d67630aba008ec5f7f9a6d0a00582f4"},
        {"----", "SQ2", "Space Quest II (2.0D 1988-03-14 3.5\")", "85390bde8958c39830e1adbe9fff87f3"},
        {"----", "SQ2", "Space Quest II (2.0A 1987-11-06 5.25\")", "ad7ce8f800581ecc536f3e8021d7a74d"},
        {"----", "SQ2", "Space Quest II (2.0A 1987-11-06 3.5\")", "6c25e33d23b8bed42a5c7fa63d588e5c"},
        {"----", "SQ2", "Space Quest II (2.0C/A 5.25\"/ST)", "bd71fe54869e86945041700f1804a651"},
        {"----", "SQ2", "Space Quest II (2.0F 1989-01-05 3.5\")", "28add5125484302d213911df60d2aded"},
        {"----", "XMAS", "Christmas Card (1986-11-13 [version 1])", "3067b8d5957e2861e069c3c0011bd43d"},
        {"----", "DEMO3", "Demo 3 1988-09-13", "289c7a2c881f1d973661e961ced77d74"},
        {"----", "BC", "The Black Cauldron (2.10 1988-11-10 5.25\")", "0c5a9acbcc7e51127c34818e75806df6"},
        {"----", "BC", "The Black Cauldron (2.10 1988-11-10 3.5\")", "0de3953c9225009dc91e5b0d1692967b"},
        {"----", "GR", "Gold Rush! (2.01 1988-12-22 5.25\")", "db733d199238d4009a9e95f11ece34e9"},
        {"----", "GR", "Gold Rush! (2.01 1988-12-22 3.5\")", "6a285235745f69b4b421403659497216"},
        {"----", "GR", "Gold Rush! (2.01 1988-12-22)", "3ae052117feb483f01a9017025fbb366"},
        {"----", "GR", "Gold Rush! (2.01 1988-12-22)", "1ef85c37fcf7224f9731f20f169c8c53"},
        {"----", "GR", "Gold Rush! (3.0 1998-12-22 3.5\")", "6882b6090473209da4cd78bb59f78dbe"},
        {"----", "KQ4", "King's Quest IV (2.0 1988-07-27)", "f50f7f997208ca0e35b2650baec43a2d"},
        {"----", "KQ4", "King's Quest IV (2.0 1988-07-27 3.5\")", "fe44655c42f16c6f81046fdf169b6337"},
        {"----", "KQ4", "King's Quest IV (2.2 1988-09-27 3.5\")", "7470b3aeb49d867541fc66cc8454fb7d"},
        {"----", "KQ4", "King's Quest IV (2.3 1988-09-27)", "6d7714b8b61466a5f5981242b993498f"},
        {"----", "KQ4", "King's Quest IV (2.3 1988-09-27 3.5\")", "82a0d39af891042e99ac1bd6e0b29046"},
        {"----", "KQ4", "King's Quest IV Demo (1988-12-20)", "a3332d70170a878469d870b14863d0bf"},
        {"----", "MH1", "Manhunter: New York (1.22 1988-08-31)", "0c7b86f05fe02c2e26cff1b07450b82a"},
        {"----", "MH1", "Manhunter: New York (1.22 1988-08-31)", "5b625329021ad49fd0c1d6f2d6f54bba"},
        {"----", "MH2", "Manhunter: San Francisco (3.02 1989-07-26 5.25\")", "bbb2c2f88d5740f7437fb7aa6f080b7b"},
        {"----", "MH2", "Manhunter: San Francisco (3.02 1989-07-26 3.5\")", "6fb6f0ee2437704c409cf17e081ba152"},
        {"----", "MH2", "Manhunter: San Francisco (3.03 1989-08-17 5.25\")", "b90e4795413c43de469a715fb3c1fa93"},
        {"----", "2PLAYD", "2 Player Demo", "4279f46b3cebd855132496476b1d2cca"},
        {"-PV-", "ABRAH", "Abrah, L'Orphelin De L'Espace (v1.2)", "b7b6d1539e14d5a26fa3088288e1badc"},
        {"----", "AEF","Advanced Epic Fighting", "6454e8c82a7351c8eef5927ad306af4f"},
        {"----", "COMBAT","AGI Combat", "0be6a8a9e19203dcca0067d280798871"},
        {"----", "COMBAT","AGI Combat (Beta)", "341a47d07be8490a488d0c709578dd10"},
        {"----", "CNTST1","AGI Contest 1 Template", "d879aed25da6fc655564b29567358ae2"},
        {"----", "CNTST2","AGI Contest 2 Template", "5a2fb2894207eff36c72f5c1b08bcc07"},
        {"MP--", "MOUSED", "AGI Mouse Demo (1.10)", "f4ad396b496d6167635ad0b410312ab8"},
        {"--V-", "AGI256", "AGI256 Demo", "79261ac143b2e2773b2753674733b0d5"},
        {"----", "PIANO","AGI Piano (v1.0)", "8778b3d89eb93c1d50a70ef06ef10310"},
        {"----", "AQUEST","AGI Quest (v1.46-TJ0)", "1cf1a5307c1a0a405f5039354f679814"},
        {"----", "PKTETR","PK AGI Tetris (1998)", "1afcbc25bfafded2d5fb82de9da0bd9a"},
        {"----", "TREK","AGI Trek (Demo)", "c02882b8a8245b629c91caf7eb78eafe"},
        {"----", "ACIDOP","Acidopolis", "7017db1a4b726d0d59e65e9020f7d9f7"},
        {"----", "AGNT55","Agent 0055 (v1.0)", "c2b34a0c77acb05482781dda32895f24"},
        {"----", "AG6VSN","Agent 06 vs. The Super Nazi", "136f89ca9f117c617e88a85119777529"},
        {"----", "AGENTQ","Agent Quest", "59e49e8f72058a33c00d60ee1097e631"},
        {"----", "ALPNDH","Al Pond - On Holiday (v1.0)", "a84975496b42d485920e886e92eed68b"},
        {"----", "ALPNDH","Al Pond - On Holiday (v1.1)", "7c95ac4689d0c3bfec61e935f3093634"},
        {"----", "ALPNDH","Al Pond - On Holiday (v1.3)", "8f30c260de9e1dd3d8b8f89cc19d2633"},
        {"----", "ALPND1","Al Pond 1 - Al Lives Forever (v1.0)", "e8921c3043b749b056ff51f56d1b451b"},
        {"----", "ALPND1","Al Pond 1 - Al Lives Forever (v1.3)", "fb4699474054962e0dbfb4cf12ca52f6"},
        {"M---", "APOCAQ","Apocalyptic Quest (v0.03 Teaser)", "42ced528b67965d3bc3b52c635f94a57"},
        {"M---", "APOCAQ","Apocalyptic Quest Demo (2003-06-24)", "c68c49a37eaac73e5aa80ce7f05bbd72"},
        {"M---", "APOCAQ","Apocalyptic Quest (4.00 Alpha 1)", "e15581628d84949b8d352d224ec3184b"},
        {"M---", "APOCAQ","Apocalyptic Quest (4.00 Alpha 2)", "30c74d194840abc3fb1341b567743ac3"},
        {"M---", "APOCAQ","Apocalyptic Quest (4.00 Alpha 2)", "0eee850005860e46345b38fea093d194"},
        {"M---", "BANDQ","Band Quest Demo", "7326abefd793571cc17ed0db647bdf34"},
        {"M---", "BANDQ","Band Quest Early Demo", "de4758dd34676b248c8301b32d93bc6f"},
        {"----", "BTITAN","Beyond the Titanic 2", "9b8de38dc64ffb3f52b7877ea3ebcef9"},
        {"----", "BIRIQ1","Biri Quest 1", "1b08f34f2c43e626c775c9d6649e2f17"},
        {"----", "BOBFBY","Bob The Farmboy", "e4b7df9d0830addee5af946d380e66d7"},
        {"M---", "BORMN1", "Boring Man 1 - The Road to Robinland", "d74481cbd227f67ace37ce6a5493039f"},
        {"M---", "BORMN2", "Boring Man 2 - Ho Man! This Game Sucks!", "250032ba105bdf7c1bc4fed767c2d37e"},
        {"----", "BOTZ","Botz", "a8fabe4e807adfe5ec02bfec6d983695"},
        {"----", "BRIANQ","Brian's Quest (v1.0)", "0964aa79b9cdcff7f33a12b1d7e04b9c"},
        {"----", "CPU21","CPU-21 (v1.0)", "35b7cdb4d17e890e4c52018d96e9cbf4"},
        {"----", "CARDRV","Car Driver (v1.1)", "2311611d2d36d20ccc9da806e6cba157"},
        {"----", "CLOAK","Cloak of Darkness (v1.0)", "5ba6e18bf0b53be10db8f2f3831ee3e5"},
        {"----", "COCOEN","Coco Coq In Grostesteing's Base (v.1.0.3)", "97631f8e710544a58bd6da9e780f9320"},
        {"----", "COCOFR","Coco Coq Dans la Base de Grostesteing (v1.0.2)", "ef579ebccfe5e356f9a557eb3b2d8649"},
        {"----", "CORBMM","Corby's Murder Mystery (v1.0)", "4ebe62ac24c5a8c7b7898c8eb070efe5"},
        {"----", "DGADGM","DG: The Adventure Game (v1.1)", "0d6376d493fa7a21ec4da1a063e12b25"},
        {"----", "DGADGM","DG: The Adventure Game (v1.1)", "258bdb3bb8e61c92b71f2f456cc69e23"},
        {"M---", "DGAGMS","DG - The AGIMouse Adventure (English v1", "efe453b92bc1487ea69fbebede4d5f26"},
        {"----", "DASHIK","Dashiki (16 Colors)", "9b2c7b9b0283ab9f12bedc0cb6770a07"},
        {"--V-", "DSH256","Dashiki - 256 Colors", "c68052bb209e23b39b55ff3d759958e6"},
        {"----", "DATEQ1","Date Quest 1 (v1.0)", "ba3dcb2600645be53a13170aa1a12e69"},
        {"----", "DATEQ2","Date Quest 2 (v1.0 Demo)", "1602d6a2874856e928d9a8c8d2d166e9"},
        {"----", "DATEQ2","Date Quest 2 (v1.0)", "f13f6fc85aa3e6e02b0c20408fb63b47"},
        {"----", "DAVESQ","Dave's Quest (v0.07)", "f29c3660de37bacc1d23547a167f27c9"},
        {"----", "DAVESQ","Dave's Quest (v0.17)", "da3772624cc4a86f7137db812f6d7c39"},
        {"----", "DISCON","Disco Nights (Demo)", "dc5a2b21182ba38bdcd992a3a978e690"},
        {"----", "DOGSQ","Dogs Quest - The Quest for the Golden Bone (v1.0)", "f197357edaaea0ff70880602d2f09b3e"},
        {"----", "DRJUMM","Dr. Jummybummy's Space Adventure", "988bd81785f8a452440a2a8ac67f96aa"},
        {"----", "EDWARD","Ed Ward", "98be839b9f30cbedea4c9cee5442d827"},
        {"----", "ELFINT","Elfintard", "c3b847e9e9e978af9708df76a0751dc2"},
        {"----", "ENCLOS","Enclosure (v1.01)", "f08e66fee9ecdde77db7ee9a10c96ba2"},
        {"----", "ENCLOS","Enclosure (v1.03)", "e4a0613ed02401502e506ba3565a8c40"},
        {"----", "EPICFT","Epic Fighting (v0.1)", "aff24a1b3bdd676187685c4d95ba4294"},
        {"----", "ESCAPE","Escape Quest (v0.0.3)", "2346b65619b1da0298b715b06d1a45a1"},
        {"----", "EDESRT","Escape from the Desert (beta 1)", "dfdc634d340854bd6ece28024010758d"},
        {"----", "ESALES","Escape from the Salesman", "e723ca4fe0f6f56affe039fbb4dbeb6c"},
        {"----", "FLAGQ", "Flag Quest", "ca062c27e6f932661cc06ed986663861"},
        {"----", "FQ2","Fu$k Quest 2 - Romancing the Bone (Teaser)", "d288355d71d9bb1639260ccaa3b2fbfe"},
        {"----", "FQ2","Fu$k Quest 2 - Romancing the Bone", "294beeb7765c7ea6b05ed7b9bf7bff4f"},
        {"----", "GTAMP1","Gennadi Tahab Autot - Mission Pack 1 - Kuressaare", "bfa5fe71978e6ccf3d4eedd430124015"},
        {"----", "GOWEST","Go West, Young Hippie", "ff31484ea465441cb5f3a0f8e956b716"},
        {"----", "GOODMN","Good Man (demo v3.41)", "3facd8a8f856b7b6e0f6c3200274d88c"},
        {"----", "GOODMN","Good Man (demo v4.0)", "d36f5d98cfcfd28cf7d4103906c59a77"},
        {"----", "GOODMN","Good Man (demo v4.0T)", "8184f70a5a33d4f407dfc8e9ddab99e9"},
        {"M---", "HLFDTH","Half-Death - Terror At White-Mesa", "b62c05d0ace878261392073f57ae788c"},
        {"----", "HANKQE","Hank's Quest (v1.0 English) - Victim of Society", "64c15b3d0483d17888129100dc5af213"},
        {"----", "HANKQE","Hank's Quest (v1.1 English) - Victim of Society", "86d1f1dd9b0c4858d096e2a60cca8a14"},
        {"----", "HANKQD","Hank's Quest (v1.81 Dutch) - Slachtoffer Van Het Gebeuren", "41e53972d55ff3dff9e90d15fe1b659f"},
        {"----", "HANKQE","Hank's Quest (v1.81 English) - Victim of Society", "7a776383282f62a57c3a960dafca62d1"},
        {"----", "HERBAO","Herbao (v0.2)", "6a5186fc8383a9060517403e85214fc2"},
        {"M---", "HITLER","Hitler's Legacy (v.0004q)", "a412881269ba34584bd0a3268e5a9863"},
        {"----", "GOURDB","Gourd of the Beans", "4a1c1ef3a7901baf0ab45fde0cfadd89"},
        {"----", "JJVAMP","Jack & Julia - VAMPYR", "8aa0b9a26f8d5a4421067ab8cc3706f6"},
        {"----", "JEFFSQ","Jeff's Quest (v.5 alpha Jun 1)", "10f1720eed40c12b02a0f32df3e72ded"},
        {"----", "JEFFSQ","Jeff's Quest (v.5 alpha May 31)", "51ff71c0ed90db4e987a488ed3bf0551"},
        {"----", "JENSQ","Jen's Quest (Demo 1)", "361afb5bdb6160213a1857245e711939"},
        {"----", "JENSQ","Jen's Quest (Demo 2)", "3c321eee33013b289ab8775449df7df2"},
        {"----", "JIGGY","Jiggy Jiggy Uh! Uh!", "bc331588a71e7a1c8840f6cc9b9487e4"},
        {"----", "JIMMY","Jimmy In: The Alien Attack (v0.1)", "a4e9db0564a494728de7873684a4307c"},
        {"-P--", "JOLIMI","Jolimie, le Village Maudit (v1.1)", "68d7aef1161bb5972fe03efdf29ccb7f"},
        {"----", "JOEMCM","Joe McMuffin In \"What's Cooking, Doc\" (v1.0)", "8a3de7e61a99cb605fa6d233dd91c8e1"},
        {"----", "JOCHEF","Journey Of Chef", "aa0a0b5a6364801ae65fdb96d6741df5"},
        {"----", "JUKEBX","Jukebox (v1.0)", "c4b9c5528cc67f6ba777033830de7751"},
        {"----", "JUSTIN","Justin Quest (v1.0 in development)", "103050989da7e0ffdc1c5e1793a4e1ec"},
        {"----", "JOULUM","Jõulumaa (v0.05)", "53982ecbfb907e41392b3961ad1c3475"},
        {"----", "KQ2BI","King's Quest 2  - Breast Intentions (v2.0 Mar 26)", "a25d7379d281b1b296d4785df90a8e78"},
        {"----", "KQ2BI","King's Quest 2  - Breast Intentions (v2.0 Aug 16)", "6b4f796d0421d2e12e501b511962e03a"},
        {"----", "LASSEH","Lasse Holm: The Quest for Revenge (v1.0)", "f9fbcc8a4ef510bfbb92423296ff4abb"},
        {"----", "LAWMAN","Lawman for Hire", "c78b28bfd3767dd455b992cd8b7854fa"},
        {"----", "LEFTYV","Lefty Goes on Vacation (Not in The Right Place)", "ccdc49a33870310b01f2c48b8a1f3c34"},
        {"----", "INSEPA","Les Inseparables (v1.0)", "4b780887cab0ecabc5eca319acb3acf2"},
        {"M---", "ISABEL","Isabella Coq - A Present For My Dad", "55c6819f2330c4d5d6459874c9f123d9"},
        {"----", "LPIRAT","Little Pirate (Demo 2 v0.6)", "437068efe4ec32d436da09d6f2ea56e1"},
        {"----", "LSTETY","Lost Eternity (v1.0)", "95f15c5632feb8a39e9ca3d9af35fcc9"},
        {"----", "MDQUST","MD Quest - The Search for Michiel (v0.10)", "2a6fcb21d2b5e4144c38ed817fabe8ee"},
        {"----", "MAALEA","Maale Adummin Quest", "ddfbeb33feb7cf78504fe4dba14ec63b"},
        {"----", "MONKEY","Monkey Man", "2322d03f997e8cc235d4578efff69cfa"},
        {"----", "NATU1E","Naturette 1 (English v1.2)", "0a75884e7f010974a230bdf269651117"},
        {"----", "NATU1E","Naturette 1 (English v1.3)", "f15bbf999ac55ebd404aa1eb84f7c1d9"},
        {"----", "NATU1F","Naturette 1 (French v1.2)", "d3665622cc41aeb9c7ecf4fa43f20e53"},
        {"MP--", "NATU2E","Naturette 2 - Daughter of the Moon (v1.0)", "bdf76a45621c7f56d1c9d40292c6137a"},
        {"M---", "NATU3E","Naturette 3 - Adventure in Treeworld (v1.0a)", "6dbb0e7fc75fec442e6d9e5a06f1530e"},
        {"M---", "NATU4", "Naturette 4", "8253706b6ef5423a79413b216760297c"},
        {"----", "HANGMN","New AGI Hangman Test", "d69c0e9050ccc29fd662b74d9fc73a15"},
        {"----", "NAPALM","Napalm Quest (v0.5)", "b659afb491d967bb34810d1c6ce22093"},
        {"----", "NICKQ","Nick's Quest - In Pursuit of QuakeMovie (v2.1 Gold)", "e29cbf9222551aee40397fabc83eeca0"},
        {"MP--", "OPENMN","Open Mic Night (v0.1)", "70000a2f67aac27d1133d019df70246d"},
        {"----", "OPRECN","Operation: Recon", "0679ce8405411866ccffc8a6743370d0"},
        {"----", "PATQST","Patrick's Quest (Demo v1.0)", "f254f5b894b98fec5f92acc07fb62841"},
        {"----", "PHANTA","Phantasmagoria", "87d20c1c11aee99a4baad3797b63146b"},
        {"----", "PHARQ","Pharaoh Quest (v0.0)", "51c630899d076cf799e573dadaa2276d"},
        {"----", "PHILQ","Phil's Quest - the Search for Tolbaga", "5e7ca45c360e03164b8358e49900c588"},
        {"----", "PINKUN","Pinkun Maze Quest (v0.1)", "148ff0843af389928b3939f463bfd20d"},
        {"----", "PIRATQ","Pirate Quest", "bb612a919ed2b9ea23bbf03ce69fed42"},
        {"----", "POTHDQ","Pothead Quest (v0.1)", "d181101385d3a45082f418cd4b3c5b01"},
        {"----", "PRESQ","President's Quest", "4937d0e8ecadb7888faeb347799b0388"},
        {"----", "PRINCE","Prince Quest", "266248d75c3130c8ccc9c9bf2ad30a0d"},
        {"----", "PROFEN","The Professor is Missing (Mar 17)", "6232de31cc204affdf2e92dfe3dc0e4d"},
        {"----", "PROFEN","The Professor is Missing (Mar 22)", "b5fcf0ca2f0d1c073be82f01e2170961"},
        {"----", "PROFFR","Le Professeur a Disparu", "7d9f8a4d4610bb9b0b97caa17590c2d3"},
        {"----", "QFG6","Quest for Glory VI - Hero's Adventure", "d26765c3075064c80d284c5e06e33a7e"},
        {"----", "QFHOME","Quest for Home", "d2895dc1cd3930f2489af0f843b144b3"},
        {"----", "QLADY","Quest for Ladies (demo v1.1 Apr 1)", "3f6e02f16e1154a0daf296c8895edd97"},
        {"----", "QLADY","Quest for Ladies (demo v1.1 Apr 6)", "f75e7b6a0769a3fa926eea0854711591"},
        {"----", "QPIRAC","Quest for Piracy 1 - Enter the Silver Pirate (v0.15)", "d23f5c2a26f6dc60c686f8a2436ea4a6"},
        {"----", "QRDEAL","Quest for a Record Deal", "f4fbd7abf056d2d3204f790da5ac89ab"},
        {"----", "RALPHQ","Ralph's Quest (v0.1)", "5cf56378aa01a26ec30f25295f0750ca"},
        {"----", "RESI44","Residence 44 Quest (v0.99)", "7c5cc64200660c70240053b33d379d7d"},
        {"----", "RESI44","Residence 44 Quest (v0.99)", "fe507851fddc863d540f2bec67cc67fd"},
        {"----", "RESI44","Residence 44 Quest (v1.0a)", "f99e3f69dc8c77a45399da9472ef5801"},
        {"----", "SQ2EYE","SQ2Eye (v0.3)", "2be2519401d38ad9ce8f43b948d093a3"},
        {"----", "SQ2EYE","SQ2Eye (v0.41)", "f0e82c55f10eb3542d7cd96c107ae113"},
        {"----", "SQ2EYE","SQ2Eye (v0.42)", "d7beae55f6328ef8b2da47b1aafea40c"},
        {"----", "SQ2EYE","SQ2Eye (v0.43)", "2a895f06e45de153bb4b77c982009e06"},
        {"----", "SQ2EYE","SQ2Eye (v0.44)", "5174fc4b6d8a477ba0ff0575cd64e0aa"},
        {"----", "SQ2EYE","SQ2Eye (v0.45)", "6e06f8bb7b90ce6f6aabf1a0e620159c"},
        {"----", "SQ2EYE","SQ2Eye (v0.46)", "bf0ad7a035ff9113951d09d1efe380c4"},
        {"----", "SQ2EYE","SQ2Eye (v0.47)", "85dc3be1d33ff932c292b74f9037abaa"},
        {"----", "SQ2EYE","SQ2Eye (v0.48)", "587574252972a5b5c070a647973a9b4a"},
        {"----", "SQ2EYE","SQ2Eye (v0.481)", "fc9234beb49804ae869696ce5af8ef30"},
        {"----", "SQ2EYE","SQ2Eye (v0.482)", "3ed84b7b87fa6840f25c15f250a11ffb"},
        {"----", "SQ2EYE","SQ2Eye (v0.483)", "647c31298d3f9cda641231b893e347c0"},
        {"----", "SQ2EYE","SQ2Eye (v0.484)", "f2c86fae7b9046d408c62c8c49a4b882"},
        {"----", "SQ2EYE","SQ2Eye (v0.485)", "af59e36bc28f44545458b68a93e91e67"},
        {"----", "SQ2EYE","SQ2Eye (v0.486)", "3fd86436e93456770dbdd4593eded70a"},
        {"----", "SARIEN","Sarien", "314e5fdef17b803226d1de3af2e997ea"},
        {"----", "SAVSAN","Save Santa (v1.0)", "4644f6beb5802081772f14be56ae196c"},
        {"----", "SAVSAN","Save Santa (v1.3)", "f8afdb6efc5af5e7c0228b44633066af"},
        {"----", "SCHILL","Schiller (preview 1)", "ade39dea968c959cfebe1cf935d653e9"},
        {"----", "SCHILL","Schiller (preview 2)", "62cd1f8fc758bf6b4aa334e553624cef"},
        {"-P--", "SERGD1","Serguei's Destiny 1 (v1.0)", "b86725f067e456e10cdbdf5f58e01dec"},
        {"-P--", "SERGD1","Serguei's Destiny 1 (v1.1 2003 Apr 10)", "91975c1fb4b13b0f9a8e9ff74731030d"},
        {"M---", "SERGD2","Serguei's Destiny 2 (v0.1.1 Demo)", "906ccbc2ddedb29b63141acc6d10cd28"},
        {"----", "SHIFTY","Shifty (v1.0)", "2a07984d27b938364bf6bd243ac75080"},
        {"M---", "STILEG","Sliding Tile Game (v1.00)", "949bfff5d8a81c3139152eed4d84ca75"},
        {"----", "SNWBRD","Snowboarding Demo (v1.0)", "24bb8f29f1eddb5c0a099705267c86e4"},
        {"----", "SOLSYS","Solar System Tour", "b5a3d0f392dfd76a6aa63f3d5f578403"},
        {"----", "SORAPP","Sorceror's Appraisal", "fe62615557b3cb7b08dd60c9d35efef1"},
        {"----", "STREK","Space Trek (v1.0)", "807a1aeadb2ace6968831d36ab5ea37a"},
        {"----", "SPCDLV","Special Delivery", "88764dfe61126b8e73612c851b510a33"},
        {"----", "SPBIKE","Speeder Bike Challenge (v1.0)", "2deb25bab379285ca955df398d96c1e7"},
        {"----", "STARCO","Star Commander 1 - The Escape (v1.0)", "a7806f01e6fa14ebc029faa58f263750"},
        {"----", "STARPI","Star Pilot: Bigger Fish", "8cb26f8e1c045b75c6576c839d4a0172"},
        {"-P--", "STREET","Street Quest (Demo)", "cf2aa94a7eb78dce6892c37f03e310d6"},
        {"----", "TOTIKI","Tales of the Tiki", "8103c9c87e3964690a14a3d0d83f7ddc"},
        {"----", "TEXM1","Tex McPhilip 1 - Quest For The Papacy", "3c74b9a24b51aa8020ac82bee3132266"},
        {"----", "TEXM2","Tex McPhilip 2 - Road To Divinity (v1.5)", "7387e8df854440bc26620ca0ea43af9a"},
        {"----", "TEXM3","Tex McPhilip 3 - A Destiny of Sin (Demo v0.25)", "992d12031a486ad84e592ff5d7c9d782"},
        {"----", "TEXM3","Tex McPhilip 3 - A Destiny of Sin (v1.02)", "587d15e1106e59c33053c01b301ffe05"},
        {"----", "13THDI","The 13th Disciple (v1.00)", "887719ad59afce9a41ec057dbb73ad73"},
        {"----", "13THDI","The 13th Disciple (v1.01)", "58e3ec1b9ac1a79901c472aaa59db832"},
        {"----", "CZHERM","The Adventures of a Crazed Hermit", "6e3086cbb794d3299a9c5a9792295511"},
        {"----", "GOURDB","The Gourd of the Beans", "246f4d94946afb547482d44a53616d06"},
        {"----", "GOURDB","The Gourd of the Beans", "5fe4b1fdcccd6b6a7c87d852a67c7f1e"},
        {"----", "GRDEAD","The Grateful Dead", "c2146631afacf8cb455ce24f3d2d46e7"},
        {"----", "LSHAYL","The Legend of Shay-Larah 1 - The Lost Prince", "04e720c8e30c9cf12db22ea14a24a3dd"},
        {"----", "LZELDA","The Legend of Zelda: The Fungus of Time (Demo v1.00)", "dcaf8166ceb62a3d9b9aea7f3b197c09"},
        {"----", "HARRYS","The Legendary Harry Soupsmith (Demo 1998 Apr 2)", "64c46b0d6fc135c9835afa80980d2831"},
        {"----", "HARRYS","The Legendary Harry Soupsmith (Demo 1998 Aug 19)", "8d06d82970f2c591d880a95476efbcf0"},
        {"----", "LHDUDE","The Long Haired Dude: Encounter of the 18-th Kind", "86ea17b9fc2f3e537a7e40863d352c29"},
        {"----", "LOSTPL","The Lost Planet (v0.9)", "590dffcbd932a9fbe554be13b769cac0"},
        {"----", "LOSTPL","The Lost Planet (v1.0)", "58564df8b6394612dd4b6f5c0fd68d44"},
        {"----", "TNAORW","The New Adventure of Roger Wilco (v1.00)", "e5f0a7cb8d49f66b89114951888ca688"},
        {"----", "RUBY","The Ruby Cast (v0.02)", "ed138e461bb1516e097007e017ab62df"},
        {"----", "SHWPLN","The Shadow Plan", "c02cd10267e721f4e836b1431f504a0a"},
        {"----", "SORAPP","The Sorceror's Appraisal", "b121ba95d2beb6c16e2f762a13b8baa2"},
        {"----", "TIMEQ","Time Quest (Demo v0.1)", "12e1a6f03ea4b8c5531acd0400b4ed8d"},
        {"----", "TIMEQ","Time Quest (Demo v0.2)", "7b710608abc99e0861ac59b967bf3f6d"},
        {"----", "TOBYWD","Toby's World (Demo)", "3f8ebea0eb32303e65e2a6e8341c6741"},
        {"----", "TTSCB","Tonight The Shrieking Corpses Bleed (Demo v0.11)", "bcc57a7c8d563fa0c333107ae1c0a6e6"},
        {"----", "TTSCB","Tonight The Shrieking Corpses Bleed (v1.01)", "36b38f621b38e8d104aa0807302dc8c9"},
        {"----", "TURKSQ","Turks' Quest - Heir to the Planet", "3d19254b737c8b218e5bc4580542b79a"},
        {"----", "UAGIFG","Ultimate AGI Fangame (Demo)", "2d14d6fa2a2136d681e46e06821905bf"},
        {"----", "URIQ","URI Quest (v0.173 Feb 27)", "3986eefcf546dafc45f920ae91a697c3"},
        {"----", "URIQ","URI Quest (v0.173 Jan 29)", "494150940d34130605a4f2e67ee40b12"},
        {"----", "VTGADV","V - The Graphical Adventure", "c71f5c1e008d352ae9040b77fcf79327"},
        {"----", "VDGIRL","Voodoo Girl - Queen of the Darned (v1.2 2002 Jan 1)", "ae95f0c77d9a97b61420fd192348b937"},
        {"----", "VDGIRL","Voodoo Girl - Queen of the Darned (v1.2 2002 Mar 29)", "11d0417b7b886f963d0b36789dac4c8f"},
        {"----", "WIZARO","Wizaro (v0.1)", "abeec1eda6eaf8dbc52443ea97ff140c"},
        {"----", "TETRIS", "AGI Tetris", "7a874e2db2162e7a4ce31c9130248d8a"},
        {"----", "CLDEST", "Caitlyn's Destiny Demo", "5b8a3cdb2fc05469f8119d49f50fbe98"},
        {"----", "CLDEST", "Caitlyn's Destiny", "818469c484cae6dad6f0e9a353f68bf8"},
        {"----", "GOUTSQ", "Get Outta Space Quest", "aaea5b4a348acb669d13b0e6f22d4dc9"},
        {"----", "SQ0", "Space Quest 0 (v1.03)", "d2fd6f7404e86182458494e64375e590"},
        {"----", "SQ0", "Space Quest 0 (v1.04)", "2ad9d1a4624a98571ee77dcc83f231b6"},
        {"----", "SQ0", "Space Quest 0", "e1a8e4efcce86e1efcaa14633b9eb986"},
        {"----", "SQX", "Space Quest X (v10.0 Feb 05)", "c992ae2f8ab18360404efdf16fa9edd1"},
        {"----", "SQX", "Space Quest X (v10.0 Jul 18)", "812edec45cefad559d190ffde2f9c910"},
        {"----", "SQX", "Space Quest X (v10.0)", "f0a59044475a5fa37c055d8c3eb4d1a7"}
    };
}
