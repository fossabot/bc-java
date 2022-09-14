package org.bouncycastle.pqc.jcajce.provider.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import junit.framework.TestCase;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.pqc.jcajce.interfaces.DilithiumKey;
import org.bouncycastle.pqc.jcajce.interfaces.DilithiumPrivateKey;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public class DilithiumTest
    extends TestCase
{
    byte[] msg = Strings.toByteArray("Hello World!");

    public void setUp()
    {
        if (Security.getProvider(BouncyCastlePQCProvider.PROVIDER_NAME) == null)
        {
            Security.addProvider(new BouncyCastlePQCProvider());
        }
    }

    public void testPrivateKeyRecovery()
            throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", "BCPQC");

        kpg.initialize(DilithiumParameterSpec.dilithium3, new DilithiumTest.RiggedRandom());

        KeyPair kp = kpg.generateKeyPair();

        KeyFactory kFact = KeyFactory.getInstance("Dilithium", "BCPQC");

        DilithiumKey privKey = (DilithiumKey)kFact.generatePrivate(new PKCS8EncodedKeySpec(kp.getPrivate().getEncoded()));

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bOut);

        oOut.writeObject(privKey);

        oOut.close();

        ObjectInputStream oIn = new ObjectInputStream(new ByteArrayInputStream(bOut.toByteArray()));

        DilithiumKey privKey2 = (DilithiumKey)oIn.readObject();

        assertEquals(privKey, privKey2);

        assertEquals(kp.getPublic(), ((DilithiumPrivateKey)privKey2).getPublicKey());
        assertEquals(((DilithiumPrivateKey)privKey).getPublicKey(), ((DilithiumPrivateKey)privKey2).getPublicKey());
    }

    public void testPublicKeyRecovery()
            throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", "BCPQC");

        kpg.initialize(DilithiumParameterSpec.dilithium5, new DilithiumTest.RiggedRandom());

        KeyPair kp = kpg.generateKeyPair();

        KeyFactory kFact = KeyFactory.getInstance("Dilithium", "BCPQC");

        DilithiumKey pubKey = (DilithiumKey)kFact.generatePublic(new X509EncodedKeySpec(kp.getPublic().getEncoded()));

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(bOut);

        oOut.writeObject(pubKey);

        oOut.close();

        ObjectInputStream oIn = new ObjectInputStream(new ByteArrayInputStream(bOut.toByteArray()));

        DilithiumKey pubKey2 = (DilithiumKey)oIn.readObject();

        assertEquals(pubKey, pubKey2);
    }

    public void testDilithiumRandomSig()
            throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", "BCPQC");

        kpg.initialize(DilithiumParameterSpec.dilithium2, new SecureRandom());

        KeyPair kp = kpg.generateKeyPair();

        Signature sig = Signature.getInstance("Dilithium", "BCPQC");

        sig.initSign(kp.getPrivate(), new SecureRandom());

        sig.update(msg, 0, msg.length);

        byte[] s = sig.sign();

        sig = Signature.getInstance("Dilithium", "BCPQC");

        sig.initVerify(kp.getPublic());

        sig.update(msg, 0, msg.length);

        assertTrue(sig.verify(s));
    }

    /**
     * count = 0
     * seed = 061550234D158C5EC95595FE04EF7A25767F2E24CC2BC479D09D86DC9ABCFDE7056A8C266F9EF97ED08541DBD2E1FFA1
     * mlen = 33
     * msg = D81C4D8D734FCBFBEADE3D3F8A039FAA2A2C9957E835AD55B22E75BF57BB556AC8
     * pk = 096BA86CB658A8F445C9A5E4C28374BEC879C8655F68526923240918074D0147C03162E4A49200648C652803C6FD7509AE9AA799D6310D0BD42724E0635920186207000767CA5A8546B1755308C304B84FC93B069E265985B398D6B834698287FF829AA820F17A7F4226AB21F601EBD7175226BAB256D8888F009032566D6383D68457EA155A94301870D589C678ED304259E9D37B193BC2A7CCBCBEC51D69158C44073AEC9792630253318BC954DBF50D15028290DC2D309C7B7B02A6823744D463DA17749595CB77E6D16D20D1B4C3AAD89D320EBE5A672BB96D6CD5C1EFEC8B811200CBB062E473352540EDDEF8AF9499F8CDD1DC7C6873F0C7A6BCB7097560271F946849B7F373640BB69CA9B518AA380A6EB0A7275EE84E9C221AED88F5BFBAF43A3EDE8E6AA42558104FAF800E018441930376C6F6E751569971F47ADBCA5CA00C801988F317A18722A29298925EA154DBC9024E120524A2D41DC0F18FD8D909F6C50977404E201767078BA9A1F9E40A8B2BA9C01B7DA3A0B73A4C2A6B4F518BBEE3455D0AF2204DDC031C805C72CCB647940B1E6794D859AAEBCEA0DEB581D61B9248BD9697B5CB974A8176E8F910469CAE0AB4ED92D2AEE9F7EB50296DAF8057476305C1189D1D9840A0944F0447FB81E511420E67891B98FA6C257034D5A063437D379177CE8D3FA6EAF12E2DBB7EB8E498481612B1929617DA5FB45E4CDF893927D8BA842AA861D9C50471C6D0C6DF7E2BB26465A0EB6A3A709DE792AAFAAF922AA95DD5920B72B4B8856C6E632860B10F5CC08450003671AF388961872B466400ADB815BA81EA794945D19A100622A6CA0D41C4EA620C21DC125119E372418F04402D9FA7180F7BC89AFA54F8082244A42F46E5B5ABCE87B50A7D6FEBE8D7BBBAC92657CBDA1DB7C25572A4C1D0BAEA30447A865A2B1036B880037E2F4D26D453E9E913259779E9169B28A62EB809A5C744E04E260E1F2BBDA874F1AC674839DDB47B3148C5946DE0180148B7973D63C58193B17CD05D16E80CD7928C2A338363A23A81C0608C87505589B9DA1C617E7B70786B6754FBB30A5816810B9E126CFCC5AA49326E9D842973874B6359B5DB75610BA68A98C7B5E83F125A82522E13B83FB8F864E2A97B73B5D544A7415B6504A13939EAB1595D64FAF41FAB25A864A574DE524405E878339877886D2FC07FA0311508252413EDFA1158466667AFF78386DAF7CB4C9B850992F96E20525330599AB601D454688E294C8C3E
     * sk = 59044102F3CFBE1BE03C144102F7EF75FBEF83043F7CFC20C20BEEC007DE3F041FBF0BFF401041030C40040FAE7E103F7E100085FC013D1410C80C2F000810461C2F480BEE8017D17F07F1411BA24013C1BDF83DC407D17E07C13917F0F9044045FC40BD0FF07D07EF0003DFC1F3CFFD1FC03FEFC0B8FC6E7B0BBDBD0FE0BE17D14307EFFE0FBFC6F81FBFF43EC1F87041D42083EC3DC2F4407BF84EC4140FC403F037F3FEC013E0FEE02180082F83FBE07BFFE043F40EC6FFB1BF200007FFBFFA0FFF6FFBCE83EBFEBEFC0FFDF3F103FC6F3FF0500A18718308007D03F200E4213BF04FFD17D000F0017A17F180E04FFF07DEC2244048148E8704503EE06F86080243F81FFF03BF4003F07EF3DE02FBFFC00420C1F40FBDF0707E043FF5FFD0000430400C4F49F4207C142F80EC3E010BFF7C13F07FF85F7F17E07C17FF33FC4EC303FFBCFFEEC41830FF0831BDF45F05F06FC503B0C0F84E4013E100E7E1441450C2FBEEBC0C0FBEFC60BCFFEF3CFBDF4303EF800BF2BE0BF001F01F43F41FFE08517B001141E00144F7EF8007CEBDFFFF4213A0B9F8A0FE04103C17E0820BB1C30C30C00FFFFC00007D18017CFF90C3101E7E103040FC4FBE04213E07AF80FFEFC80FBFBD0810BCFB8FBC087FB8FFF1010C2E81002F3EF3BF01F07E41FBC07F2C0FB8F43F401C5D81FFCEBE07C07E0BF17EEBEE830C514003FF7EF3E08403D1FFFFE105F840C20BDF0607FFFEF46E7EFFF08000400DE830000F3F82EF9D82E84EFFF3CEC4E81E01002103102EFC080F3B0801041BAE42F7F040F83EC31010031BC0410FAFF9F0004010133A089FFEF7BE8317A0020FEF010052BA04107E100F821C2F41F44F4EF7B000F02E41F82F380830FE08A1F707FF82EC7F42E81004041103E8307B13D0FDFF8F830F9FC5FFCD7E040F410FFFB9F423750860C11C5FFA144EC0080F02DC0F420820450790020BCF80EFFFBCEC4FBFF4200AFC00C02060C004303EF81FFA104107E4117AF01F81202FC1E44143FFE206EB3E881BB13F13920403FF7A000144102E7FFC2143E7FF4AF3F13F07E181DC317E240F4500303F2DDDCF1E1513E3EF15E8DC1309E50AEE03EFDC17081706FD03E6ECE4F30EBD1909051906E90CE806EB0B19E719EFFBF10D0DF1DC0CF6F1F4F8FEFBE9F9550E2107FCDCCBDFE9F4F7EE1AF8142115F910002AF2F5FF141ADA220AECFE040CEF0B29EB201930F2D3E401E5DEEFF4DDEA17F1FE141217F81C36050109F8F61F02DD19F90310C7F40208E9052C3942F8FFF2CCF9FDF83CFA12DC091C0D02F00411F5281E40D7F92DBA11D73D04C10BFD13E617110AF3ED05F6CFE705E0F70E1FF80533FC120C002CE81FF52638190FE3FED6F0FBBB23E6F408EF32220B13DD27F007E5FA00D72614F0E302210707EC111E070E2A032DF91DE3FCE800F1F9F2F7FE170101180412CBD1E90019F2011522DAEAED13F8E5F425DCEF24E01CE614E7DCEC01F2F4F914F4010107ED26E2E9DF0BF5F007EA07FAFBC6D7E607FAFCFD270DFD0D17FC4EF0EE00071AECDE09F8F215E113F80209CCF308D7E6251ECE0EDFED0CC9F4050B2714F61BF703F0EBF104010DEBFBF21AFC1BF01823FEDEFAF7F807E3F3020AEB01FE19EEE8E90D00E5FAED1EFDF628E5F0E6F0FC13F4FB05FB0B09EA0A0E08EE13293212E90CE4FEF223F4FF030BEBED1B402ED2F6171102BC0CF9E9F335ED0C01FAF0FEFAE41DF0050A162C11171CD90BEE211218EDFAFA0F03F4171412F319D60B01FAEE1F2823F0D6EF12D6DFEAFBFC170DECDA06E7CED500031E
     * sm = 026833B3C07507E4201748494D832B6EE2A6C93BFF9B0EE343B550D1F85A3D0DE0D704C6D17842951309D81C4D8D734FCBFBEADE3D3F8A039FAA2A2C9957E835AD55B22E75BF57BB556AC8290765843D1E460D17A527D2BCA405BD55BBC7DA09A8C620BE0AF4A767D9DB96B80F55E466676751EAABA7B93B86D71132DAA0EB376782B9EEE37519CE10FDD33FE9F29312C31D8736206D165CF4C528AA3DDC017845E1F0DD5B0A44FF961C42D874A95533E5B438982F524CA954D87533BFBE42C63FF2ABC77A34C79DB55A99171BBCB72C842A6530AF2F753F0C34AC632F9F1E7949F0BF6C67665B27722A8857D626B6FF1A136D923A39F4069B7477FF946E5247A6627791D49B59EDC9E2525A860E6E9828D18F64A9F17222E8166A02453859BBDA0B8186D8C9928BB571E4146401D7430E225904673AD21CCAC54C146C248A1DD69AB6491E901D6D71B152155BE97DE057F3916A3F1B4273308C29B2F4D9697167B90681B1583ED930A71E990467DEA368134BECEEBD597F9BEC922E816F1B0570D728F4AE0464C1F797657F87A4E52DCDCAEB9272662EA66D7C6CD8781B31AF555AD93F5F65E75816CB8DC306BB67E592B5261BACA7C509629EA2AF8ABB80CBA89EE535B76DFD9CCBBE3BF48F2BC8AA34B26E1103291053F5CB8DE3A45AFA5A76DF8B2122ED2C82FBCF2259290D41A14F86B12F35F5D49762B34CFF13EE7E42EDEC70201D7F37C33316288FA3078E36E58108865C3CFE263D563692043DECC62F3426F86061285B7B1B336F56FF41BB65E9CD6D9B92FD90F864AA1C923CB8C755F5CDE1770D862595427149D7721AAAB5D194AEA9ACDECA15BE43CBA6A62B5A33909E9FC4DA1C5814FBD7CD6A2FA572E318B42C6C319140B86E66392580A11A2B431F44C1F9270E4F7B2490F3B325A9977A71A575915636635B9969DBD6D220B24C3D99CEBBBD834B88222BD08C3ABE124E80
     * @throws Exception
     */
    public void testDilithiumKATSig()
            throws Exception
    {
        byte[] pubK = Hex.decode("1C0EE1111B08003F28E65E8B3BDEB037CF8F221DFCDAF5950EDB38D506D85BEF6177E3DE0D4F1EF5847735947B56D08E841DB2444FA2B729ADEB1417CA7ADF42A1490C5A097F002760C1FC419BE8325AAD0197C52CED80D3DF18E7774265B289912CECA1BE3A90D8A4FDE65C84C610864E47DEECAE3EEA4430B9909559408D11A6ABDB7DB9336DF7F96EAB4864A6579791265FA56C348CB7D2DDC90E133A95C3F6B13601429F5408BD999AA479C1018159550EC55A113C493BE648F4E036DD4F8C809E036B4FBB918C2C484AD8E1747AE05585AB433FDF461AF03C25A773700721AA05F7379FE7F5ED96175D4021076E7F52B60308EFF5D42BA6E093B3D0815EB3496646E49230A9B35C8D41900C2BB8D3B446A23127F7E096D85A1C794AD4C89277904FC6BFEC57B1CDD80DF9955030FDCA741AFBDAC827B13CCD5403588AF4644003C2265DFA4D419DBCCD2064892386518BE9D51C16498275EBECF5CDC7A820F2C29314AC4A6F08B2252AD3CFB199AA42FE0B4FB571975C1020D949E194EE1EAD937BFB550BB3BA8E357A029C29F077554602E1CA2F2289CB9169941C3AAFDB8E58C7F2AC77291FB4147C65F6B031D3EBA42F2ACFD9448A5BC22B476E07CCCEDA2306C554EC9B7AB655F1D7318C2B7E67D5F69BEDF56000FDA98986B5AB1B3A22D8DFD6681697B23A55C96E8710F3F98C044FB15F606313EE56C0F1F5CA0F512E08484FCB358E6E528FFA89F8A866CCFF3C0C5813147EC59AF0470C4AAD0141D34F101DA2E5E1BD52D0D4C9B13B3E3D87D1586105796754E7978CA1C68A7D85DF112B7AB921B359A9F03CBD27A7EAC87A9A80B0B26B4C9657ED85AD7FA2616AB345EB8226F69FC0F48183FF574BCD767B5676413ADB12EA2150A0E97683EE54243C25B7EA8A718606F86993D8D0DACE834ED341EEB724FE3D5FF0BC8B8A7B8104BA269D34133A4CF8300A2D688496B59B6FCBC61AE96062EA1D8E5B410C5671F424417ED693329CD983001FFCD10023D598859FB7AD5FD263547117100690C6CE7438956E6CC57F1B5DE53BB0DC72CE9B6DEAA85789599A70F0051F1A0E25E86D888B00DF36BDBC93EF7217C45ACE11C0790D70E9953E5B417BA2FD9A4CAF82F1FCE6F45F53E215B8355EF61D891DF1C794231C162DD24164B534A9D48467CDC323624C2F95D4402FF9D66AB1191A8124144AFA35D4E31DC86CAA797C31F68B85854CD959C4FAC5EC53B3B56D374B888A9E979A6576B6345EC8522C9606990281BF3EF7C5945D10FD21A2A1D2E5404C5CF21220641391B98BCF825398305B56E58B611FE5253203E3DF0D22466A73B3F0FBE43B9A62928091898B8A0E5B269DB586B0E4DDEF50D682A12D2C1BE824149AA254C6381BB412D77C3F9AA902B688C81715A59C839558556D35ED4FC83B4AB18181F40F73DCD76860D8D8BF94520237C2AC0E463BA09E3C9782380DC07FE4FCBA340CC2003439FD2314610638070D6C9EEA0A70BAE83B5D5D3C5D3FDE26DD01606C8C520158E7E5104020F248CEAA666457C10AEBF068F8A3BD5CE7B52C6AF0ABD5944AF1AD4752C9113976083C03B6C34E1D47ED69644CAD782C2F7D05F8A148961D965FA2E1723A8DDEBC22A90CD783DD1F4DB38FB9AE5A6714B3D946781643D317B7DD79381CF789A9588BB3E193B92A0B60D6B07D047F6984B0609EC57543C394CA8D5E5BCC2A731A79618BD1E2E0DA8704AF98F20F5F8F5452DDF646B95B341DD7F0D2CC1FA15BD9895CD5B65AA1CB94B5E2E788FDA9825B656639193D98328154A4F2C35495A38B6EA0D2FFAAA35DF92C203C7F31CBBCA7BD03C3C2302190CECD161FD49237E4F839E3F3");
        byte[] privK = Hex.decode("1C0EE1111B08003F28E65E8B3BDEB037CF8F221DFCDAF5950EDB38D506D85BEF394D1695059DFF40AE256C5D5EDABFB69F5F40F37A588F50532CA408A8168AB187D0AD11522110931494BF2CAEAE36979711BC585B32F08C78496F379D604D53C0A6711A966C11312AD9A821D8086542A600A4B42C1940720242628106210A43852331709308108B188C022492C1B28412C4218B042181C8610248059C9201C0348819326C582046891868A2C28D82346A1C094200A28CE3A6491C112CC24812E0902191985062C084622451CA062C64240E1BB3312496854B4606DB2668C38268441046C9B6211404811445502442084422710B92459AA0811A91709C241003957004C504C82692D29200C0B260C0A26809190AA2300E188969E0008DD84862DA14712018051907440412409B1240118010D142819928508B1091022464A0206D1246211C838C1B4769010690CC062481846920982C24120521B15041360298446ED1A63111056AD3A840CAA84C62B00003134A53344614194004C54CE306695AB08961168ECB10808B168ED990640B94602483851AB30454262251B8251C424A0B814842C4445A102023808409B7254CC64814854D19380E601651D8326A0A918908C170E0964D18468C01328D91C4054A0061230868A2104210A8611306218A248E620689C9B24508278451200D980466DC42054424852426282221612016090BA62C0A1144E0928158480D422210A006098B246E81288CC0248090308D8436404CA68450042494B68DA2926D18B344A00085E3B805140504A4C290842281C3262D0B2066CC903198382810166CC13445C0102224C688034632D840901C20680415289A188144988D9C206E9C302CC1B820614221080310A0C28C58128553204C0330814CA48D44C08D51404C1CA72C440865A03840DA20808106858C260DE2A88C9C4411594228C42604441426A1426408C0851101869B483199B20C80464459A88C0042089882900AB54562244812960544124600C88813A061E1284D0AB9914B962099B84400314E98128500B60183A00D14150E1881101901224A06681A498DE1A28411C63121262591A06D030524A1B6089444724334125BB42041B650D0888D0B074D1C94644C208E8B8808E0300944200549864D03134E19C9840937611A43684A80900204311C1742184080C8308EE1A241C33404A3282251247188D6FEF46712CA182872AB2919678AFF9D94E743E063A39E0C35CAF72A7F2EDA28E65858520D5D8467DE747CF340653B52C268F55413F5ADDC7D49011EC33EDD537423A84288869337AEA0781A124269071451722DB3BB8F2CE5B1552F83D2AF07F25613918A9F4E6F1257603888E589308CA5F95F07143D23BAAE17520B36B6E0E94FAF6845EB2131AEC383E63BC8644EE5F1ACCBA82F9211E57AFCBF509C1131A37466BC91B357DCBBBC14CCC319C4CC6AC75FCDC82C6596D07770C8277AD370B192A0B4E05F812E0E265D2912AA29F03FC9F72DFA69C9B1291A3FC583642B235F6991A954788347F60A0328C48ECEE51BA02DFF323ABD911667CB14549B618F1C5D250CAC9E35E071601992FBEC0BAE6F74213081404744D12F2A0E04BDB265E0924CADA40D1FA1F38ACA4606BFD4575712B8260A456FDDEEEFE7CA259BCDA97B9B939A5FD2889C9B49FB7D4E3553DEA61B3339BD0E6B16BF3BB227103BF9202E72DC502E28F7CE1559A4631F372520324E4EBA07545F78BF4D94B0E5B8BF51B8F176533D5CFEA5232F283A47605FA65DDB17C891C251011C4E98EEB6EB00CB65BA31C8F025C87A9FE02DBC10C5D83A065EBA5D7B2A19D5A1CB2C160AE166E867F2AF8C7D49D63FB83A614957FC0A3B5A5C74990E9A2B02120C7E6DE37E155FB472F50F0A45E47CF5F9D7A4C82982C9DC86AE877C3FD1885943E439FB003C7A9A42F71B4FF6F0A28B140CBDBA6E71B13AC31B23DE9EAB7837E15A69F833EB7B56A71D8BC2CAF1F2A31C345BD5F46EE013A7C689372337191DAA800C0AC6C46C9FF688B1A01347F257C474AA3D97C1D63A8C00E0A37B681673F57C1C9C8FCCD46F174C74A29D84CEB71F7E6B2F8CD2B089ED43F7C96DAE81A223418C20B16F1DF3D1A978AE28F6DF35EC559D04D20EC74B224AEA31A289B015B069E9CBBBF7CF6DE94CFB2A96E4AE3462C96003CDDA87DB561AF2CE3C0BA1D90413FDCE3CCF4390C02C1CB9F654F4820EC33015457D4A629FBF39419CAB7642D6885E103FCE0D4206CCE7C12C6FC44FA33AD0864C3371A7CBE820E3B371B656A38F2E7FF18FE4A50C8AB3F85D783FB57835CED8490B84EE0D99AF0D64C483CEB6366FF54F8AC8A40DB1AFA573A4FB326C74F0236ECEF3DA7120665CCE05DD654B5071723A8348E7CD7793513819B61CB64E1328E8B22E7664BD6B41B5710D19EA8809D4450850E907DFC4D0B75F588CECE962E9E0937CE1402446A4D2891A46E6617FB29D4FCD712606F7819ECA60F7E0D5B19E7FFB57C73C16FFEEB90038410CB9FCBB5E9D51EB3EB6297E9FF6AB7088FE2D9B237BC24CF7F8290118A5E0E00A0B903FB6375C848176CD0A8C8875CC59199CDA11A87A78F65CC404330B087571FD0633E27129FDAB5A8A1F793E52412B0083FD5C74DB3CF60C2543CE7C91B2800E40203F8D99FE5FDE5B108E7EDC80EBB9BB34986EC5C5A8F580E75752907FF0F294C866C2CF1F362E840B6881BD43219201781C63B0039A95BCFB4A0FECE569DF00523CE9C084B022B3B022242E28419796ACF0A0C995F948DBFFFD30D77ED105A3C9943C406B305BC81A6A248A291548F2A67F438D966A57D53F4B7BE15354E581BE16F7AD64D164E85787DF5849C810AFC28D06482F441B5FDE3DB2ED36DD25AA6664D4D43FFA32EDA25689C9F4A5D514FC66231C5401520922524438EF1DC78D693C9718DEBBD243312674C899F18910E389C8EBE505824BCC42CD4A9ACE193768220219011F3B1F335427BFF9E8BDED5C08711A09C2B71CB964C56A8393BFD2B56E9B6B2F513E682587DC1B8ED196066326871025628036700063176D345DE384E182D6C417A32AB11095EF59BB4D171B9CF81D17AC42664DED933CCB722C69857FFC53C8E7F2474B0CB2DFF2DDC8A5C601C84A701981199BCCF74112A6EC062C4FEB601A028AF01032ADB6BD15D4C2B9550AA850AD62CCC3A3665D5212B12E0FD5C5326A1E5EB1F10D557D94605E8E3F356E08FF7FD884ED3C4205463594C9AF2F39E4B1274695234B54EECED93F460EDF1A13C2CB4B17D322F6F79FE16F0357C1C4739863E796791F8647FABF730AB00E0DA509706D94571740F61F7BAF366D2774C9B5B8C61DD6BE9819A6028B264BB2E4AEA54B56D4ECAB5B528CE0C0C0CCDB73023352CB00445BAB6F7467B4644D4361C464FAC6B5B137D32391021B475FCB5F31774FD8ECABDF65475F25574C65559CB331F41C0F498B74DD941C344C50D8E64F9578714A32561FAACEAF78148E6DA4B566826925714B17108AFDD546385A3CD454D5CAA16960916282A47C4315CE236BD9E3255C604EBDC39772DB5CE0B236");
        byte[] msg = Hex.decode("D81C4D8D734FCBFBEADE3D3F8A039FAA2A2C9957E835AD55B22E75BF57BB556AC8");
        byte[] s = Hex.decode("AF5920774603D20E98A79AA3ABFA32B6E22519E673E37AC4AC73FE85341E2C2923C1992E1B0BBE3873D7C8FC5662F207BF58EA381CD4A3A0C062DEC45BDAF8BA0AA52BEF6FA14F3F6CF28F7620BF94A92CC27D045414A64D65C014963052802428BF3987A2D47516CA5C78AAB96B7BE11BCA5F2C5A26F3FCE3A26E8E09A2738F386F75D448F937EF19A846BD4DD949CAAF36DB5629884AF53A023E3F180FE4C0FAFF7BE5DFE4E89ADE3095A65600421461AD08C129D6CEA851BB39C0D7A7D151405689A091FA4DEBAC373CF54AE078F0AF7557BBC6F06A535AE8949E0C65308A59840072375295802D0E2CE9A3DA98426A00FF03FE80218C0EEC8EFE581CB9CC9A7D66B20645A8CD0490D3CE4F7E6FEAE9C9EB7A57F964D0EBC7C90B7A9F86300B3E8095E64D1294CFC4B4D9E272E8FA8DB5707D7004AF22DBFF9CFD4863DF573FE004341DA3CD4A3082532C2620455FA37C562BAFD5684EA128AFC79E01FC9B31E8433BAD7C029F2F13CC10592D2332E3E08B80D350463DE72750B1F806F493E143BD5FCA7D1698081B31BF876B2A1BC9DF50952D13B6C1321B1111172145A627AE0B4427B98975CBFFF7D68275754B45B682D709E168522E84FEA7DD3BB0F41505FF71926431D1A90D4CBF9A527AD4E284976FFF8BD9D6224A4F260391A987FB6DA6EE42C2A4900F407CE1F02E322475D313FBEBB68C2E05730809448A7428A5940139EBDF1B5556FCC5D42E1A13F32230CB6F0724831D0D071BBA5A6704806F475B74BA91B6E385D48620958D0AB1BF2B184E10F3E753B71337BE9EB653786785B43AC7E5C494AC1BCB043D461425B36098AC93055A0105AB8523B61D024A6E9B56A42D3C04726512AE4CFE05710446B06F694234EE4FA8FEEDDDC5F28A65EDE2EB58E965FE3627A571BC45B397ED092AB4BE00041729C4D192FE30678279D223A848CF4366E92B3F68DEE97C9B4A7FF22F937BE6C56639961DB29FA3CFECFFF293140886FFB92EBC79DAB59CEAF869C64F8EAF585CE97DD6B78F892772DB88A958CF0AB557A7FAA83FE621477E2B84497AB5A8ECF4A7BD32DFB902F05D2CA31047D0F1919ADDE1EE6DFD58E59BC4DAB3CCBBA36AAAF6AFCCC7B095CA94A195BE9A289526B588C3A9C56876FC415D521D442BAC0298D302419AD527DA249C2A660CD064213FFAD563183F37972578EEB9F70AC67AEE6CC2B71F283A95930B554738555791C25E7A399E685636D58D69CB6BE793B45C1969E7D5615627EBC32EED45440F87880D2829FA4FC871866164D259ED95D2731871017FF51894066FAE1FFA6F4B4A6F84FCFFDA09E718FA17135EDB3F48558D5BA67F9E6F0900340BD04DFE59B7BD67745884FB84AE3F8EE763D202743652D4F7333450580490B9C744935B19C1D5FB0DB5FBB461411362838037EB7EC3F63F26C893E7CC1C3B3F4767ABAE00FEB7BB99B1420BB29EA614747896D9EDCF8107FE504C9C308A8264DACE318D87CFE4761803E9A60DEFA6144AABC1F10A45B140DED754E73586C467BB7BF19EDEF25BE0C65E93C5E5EB8F880CCE4A858757F8FF56062B1067F4106F76B7007F6EA6F945047E85BD0FAD9D26994F678A0612B87CCF9C0CF9A433D889C96E4C12BE372277005B06AD127105D16D8FB142AEAE5373ABD61D9ADCFC5550D623CA3B8824B0E2E08C2BF4E2841EAC4C5DC56CF8954CF207C263F27C9F309F10307C0D84A65878425031375DD810D2D7E51098A3814350795C4A077FA40DD44F0FA7510F7C3F631407CF34F604C7B335632A20D2AD419BD7CC6D4242B1C66C35E5A5EDCCB13CA37D3B50465F3B4AAFF7E3161E7936088AE08401FD2C37D67A2FF91D3E6F08686D64BC2FC6C57106E49FA384AC22219F07EE8996CA3DFF59DCC5092A4BADBE87AEDE7F69A04C79B33BDF35D4A0E4CB4B55019CB0BF275295B93BDABEA516CA2B616A56918600B724BE7A01EC4EF54312B30D66F507815F2780FFEE7C30F8425A92252CE550FAB4E902E7B382D46DBD20EFE1BB0EF8A496873C09C4CEB0303C7F1DABA0102DE94190B6AC6DC810F72BCA3AA292FF38BD51A7FAB8509EC4FBE0EAA3C986166A674B7871155C348C477EF8CEDC832B5ABEE71A8D18D06DD0F5221160ABEB71E6E82CFABF731EA3515A76EF07B2C16C63B37F7AB73B67F005929A753E453B930C0AF432277FD77D8A1EB8022CDE9665763B014F0A672A04160B0A06F5540F4C264B7F22740690A2352DC863B588303AD51F0AE162BF79797F07B534501CBBFDB713A724AA98E19532187180CCFADC6EBE3142FA7DB66CD4DE7B9FBD4C8235686DB68CAF489AFA4E1E87AEF0CEFD8037E3A578EE62EB7F94ED5BC0B58EEA4B4C45FC56D31D29944D095AC96C29083DA2C77181D97A55FE6E903A2F2783DE0BAA5F47D704785C33E8D5C87ED61E65459167310EB7A99574EF819AE9161A3BD09634803D9E1E4EC7386D7946984517213AB9CF66AEA551CC457C39F86AF294CF7B073F563ED4DAB9419BDF004BD05C92B4E80EC3CFEAC97E1DDA554FDA625C4B9B039BAA7C5A2F6F97057792483CF5F852D4C3AC71AD50F779953DCFE2F63ED235D8E1D5345D6C6DF0555CC2631DEAD9B714BC4C16501E01261381F3679715345123388C852D57DCF1941D0911D49FEA7143FD2FC343A5075B64CCA48291DC28B83F76074589EAB217C7847840652C0E3AE278B3B6FB0D800C5E7DB79D5CB9CC1A87450C00B7677812D22EE20FDE8C1753A7FB93BA8BBB8595A6393DF54AA9CDB6E0879A26E49BD3B01513C6053A0746C8596CE5E5B225CFCA26AB8BF12F1FE0A647A9E4453039A1226194C46E8B98ACD710F18FB7EC05476C1CD8FC3112CCDDB1582B8817C18FE315353E7A47C821E9EE3A43CADE1B80D92A0AE8DCEB4DFF766A54DF3665FEFE3C252B72DAD7B1E3359E7FA25562C3E39DB521CE1874111FB090DBD38B3180AD034B57B031DC4DD6AF7C1A8AF3F6CE7EDB1A9E4B6D4A5920E3620818820659762EF7A4243F51DF2D8A900737D58105699B4E10CBCB359C7F3A4007697C482050EC33CF8041916A3B919A50D96EF0F589FD4556F30DBDD942EAB79DFA97C07E30247074352E1BF98E349CC7EFA5A1B8FCE4F18F1FAF6F07C99C321448B0395C8A9CBC466412F89C1A98BF5715842844F0E8236FA4696C4658B8FDE4425D09D67A38AC7258E5D5966F2D3FF66A0C0CE76E7F6B81A1BCD047FD3A205BF0CCAEA3B11079909C6CE5698F32E1F3409658FFA01EAECB4AE2B092B78989DAAD6623BB11F49F0F8F8699EC05661502FFCAD03CF415191A222D3C4C7B8AB0B5B9BBC2D9DCEFF7202D3F4244494F525364666974C4D9E6F5FA0001041927373D5A7680B8C1C9FE2029383B3C484D565F65799D9EA6A9ADD2DEE5E7F7F9000000000000000012243248");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Dilithium", "BCPQC");
        SecureRandom katRandom = new NISTSecureRandom(Hex.decode("061550234D158C5EC95595FE04EF7A25767F2E24CC2BC479D09D86DC9ABCFDE7056A8C266F9EF97ED08541DBD2E1FFA1"), null);

        kpg.initialize(DilithiumParameterSpec.dilithium2, katRandom);

        KeyPair kp = kpg.generateKeyPair();

        SubjectPublicKeyInfo pubInfo = SubjectPublicKeyInfo.getInstance(kp.getPublic().getEncoded());

        assertTrue(Arrays.areEqual(ASN1OctetString.getInstance(pubInfo.getPublicKeyData().getOctets()).getOctets(), pubK));

        PrivateKeyInfo privInfo = PrivateKeyInfo.getInstance(kp.getPrivate().getEncoded());
        ASN1Sequence seq = ASN1Sequence.getInstance(privInfo.parsePrivateKey());

        byte[] concKey = Arrays.concatenate(new byte[][]
            {
                ASN1BitString.getInstance(seq.getObjectAt(0)).getOctets(),
                ASN1BitString.getInstance(seq.getObjectAt(1)).getOctets(),
                ASN1BitString.getInstance(seq.getObjectAt(2)).getOctets(),
                ASN1BitString.getInstance(seq.getObjectAt(3)).getOctets(),
                ASN1BitString.getInstance(seq.getObjectAt(4)).getOctets(),
                ASN1BitString.getInstance(seq.getObjectAt(5)).getOctets()
            });

        assertTrue(Arrays.areEqual(concKey, privK));

        Signature sig = Signature.getInstance("Dilithium", "BCPQC");

        sig.initSign(kp.getPrivate(), katRandom);

        sig.update(msg, 0, msg.length);

        byte[] genS = sig.sign();

        assertTrue(Arrays.areEqual(s, genS));

        sig = Signature.getInstance("Dilithium", "BCPQC");

        sig.initVerify(kp.getPublic());

        sig.update(msg, 0, msg.length);

        assertTrue(sig.verify(s));
    }

    private static class RiggedRandom
            extends SecureRandom
    {
        public void nextBytes(byte[] bytes)
        {
            for (int i = 0; i != bytes.length; i++)
            {
                bytes[i] = (byte)(i & 0xff);
            }
        }
    }
}
