package org.liuxp.minioplus.core.common.utils;

/**
 *
 * 后缀名与ContentType对照表
 * 该工具类中保存了常见资源后缀名所对应的ContentType类型，便于返回资源数据时声明其ContentType格式。详见public String getContentType(String suffix)方法。
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class ContentTypeUtil {

	private static final String SEPARATOR = ".";

	/**
	 *
	 * 通过后缀名获取对应的ContentType
	 * 由文件的后缀名得到相应的ContentType以便浏览器识别该资源。该方法将返回ContentType类型字符串，型如“application/octet-stream”。
	 * @author 青阳龙野(kohgylw)
	 * @param suffix java.lang.String 资源的后缀名，必须以“.”开头，例如“.jpg”
	 * @return java.lang.String 传入后缀所对应的ContentType，若无对应类型则统一返回“application/octet-stream”（二进制流）
	 */
	public static String getContentType(String suffix) {

		String _suffix = suffix;

		if(!suffix.contains(SEPARATOR)){
			_suffix = SEPARATOR + suffix;
		}

		switch (_suffix) {
		case ".hta":
			return "application/hta";
		case ".tdf":
			return "application/x-tdf";
		case ".eml":
		case ".mht":
		case ".mhtml":
		case ".nws":
			return "message/rfc822";
		case ".vdx":
		case ".vsd":
		case ".vss":
		case ".vst":
		case ".vsw":
		case ".vsx":
		case ".vtx":
			return "application/vnd.visio";
		case ".csi":
			return "application/x-csi";
		case ".plt":
			return "application/x-plt";
		case ".906":
			return "application/x-906";
		case ".rnx":
			return "application/vnd.rn-realplayer";
		case ".xlw":
			return "application/x-xlw";
		case ".mp2":
			return "audio/mp2";
		case ".pl":
			return "application/x-perl";
		case ".mp3":
			return "audio/mp3";
		case ".mp1":
			return "audio/mp1";
		case ".wav":
			return "audio/wav";
		case ".rp":
			return "image/vnd.rn-realpix";
		case ".hmr":
			return "application/x-hmr";
		case ".top":
			return "drawing/x-top";
		case ".avi":
			return "video/avi";
		case ".pdf":
			return "application/pdf";
		case ".htt":
			return "text/webviewhtml";
		case ".torrent":
			return "application/x-bittorrent";
		case ".dcx":
			return "application/x-dcx";
		case ".biz":
		case ".cml":
		case ".dcd":
		case ".dtd":
		case ".ent":
		case ".fo":
		case ".math":
		case ".mml":
		case ".mtx":
		case ".rdf":
		case ".spp":
		case ".svg":
		case ".tld":
		case ".tsd":
		case ".vml":
		case ".vxml":
		case ".wsdl":
		case ".xdr":
		case ".xml":
		case ".xq":
		case ".xql":
		case ".xquery":
		case ".xsd":
		case ".xsl":
		case ".xslt":
			return "text/xml";
		case ".sam":
			return "application/x-sam";
		case ".wk3":
			return "application/x-wk3";
		case ".cdf":
			return "application/x-netcdf";
		case ".uls":
			return "text/iuls";
		case ".p12":
		case ".pfx":
			return "application/x-pkcs12";
		case ".aif":
		case ".aifc":
		case ".aiff":
			return "audio/aiff";
		case ".wk4":
			return "application/x-wk4";
		case ".lar":
			return "application/x-laplayer-reg";
		case ".sat":
			return "application/x-sat";
		case ".htm":
		case ".html":
		case ".htx":
		case ".jsp":
		case ".plg":
		case ".stm":
		case ".xhtml":
			return "text/html";
		case ".ltr":
			return "application/x-ltr";
		case ".wpl":
			return "application/vnd.ms-wpl";
		case ".anv":
			return "application/x-anv";
		case ".p10":
			return "application/pkcs10";
		case ".gl2":
			return "application/x-gl2";
		case ".css":
			return "text/css";
		case ".vpg":
			return "application/x-vpeg005";
		case ".dwg":
			return "application/x-dwg";
		case ".slk":
			return "drawing/x-slk";
		case ".wma":
			return "audio/x-ms-wma";
		case ".sty":
			return "application/x-sty";
		case ".wm":
			return "video/x-ms-wm";
		case ".cut":
			return "application/x-cut";
		case ".a11":
			return "application/x-a11";
		case ".sdw":
			return "application/x-sdw";
		case ".dgn":
			return "application/x-dgn";
		case ".cel":
			return "application/x-cel";
		case ".dxb":
			return "application/x-dxb";
		case ".tga":
			return "application/x-tga";
		case ".dxf":
			return "application/x-dxf";
		case ".mil":
			return "application/x-mil";
		case ".wmf":
			return "application/x-wmf";
		case ".latex":
			return "application/x-latex";
		case ".mdb":
			return "application/msaccess";
		case ".wks":
			return "application/x-wks";
		case ".wkq":
			return "application/x-wkq";
		case ".xap":
			return "application/x-silverlight-app";
		case ".tg4":
			return "application/x-tg4";
		case ".mp2v":
		case ".mpv2":
			return "video/mpeg";
		case ".ras":
			return "application/x-ras";
		case ".pko":
			return "application/vnd.ms-pki.pko";
		case ".m1v":
		case ".m2v":
		case ".mpe":
		case ".mps":
			return "video/x-mpeg";
		case ".wsc":
			return "text/scriptlet";
		case ".rsml":
			return "application/vnd.rn-rsml";
		case ".cdr":
			return "application/x-cdr";
		case ".001":
			return "application/x-001";
		case ".la1":
			return "audio/x-liquid-file";
		case ".asp":
			return "text/asp";
		case ".301":
			return "application/x-301";
		case ".etd":
			return "application/x-ebx";
		case ".gif":
			return "image/gif";
		case ".mns":
			return "audio/x-musicnet-stream";
		case ".dwf":
			return "Model/vnd.dwf";
		case ".asa":
			return "text/asa";
		case ".bmp":
			return "application/x-bmp";
		case ".":
			return "application/x-";
		case ".323":
			return "text/h323";
		case ".fdf":
			return "application/vnd.fdf";
		case ".pic":
			return "application/x-pic";
		case ".cot":
			return "application/x-cot";
		case ".sdp":
			return "application/sdp";
		case ".pgl":
			return "application/x-pgl";
		case ".doc":
		case ".dot":
		case ".rtf":
		case ".wiz":
			return "application/msword";
		case ".net":
			return "image/pnetvue";
		case ".m3u":
			return "audio/mpegurl";
		case ".js":
		case ".ls":
		case ".mocha":
			return "application/x-javascript";
		case ".rv":
			return "video/vnd.rn-realvideo";
		case ".sst":
			return "application/vnd.ms-pki.certstore";
		case ".wvx":
			return "video/x-ms-wvx";
		case ".rle":
			return "application/x-rle";
		case ".rlc":
			return "application/x-rlc";
		case ".rat":
			return "application/rat-file";
		case ".vda":
			return "application/x-vda";
		case ".mxp":
			return "application/x-mmxp";
		case ".r3t":
			return "text/vnd.rn-realtext3d";
		case ".lbm":
			return "application/x-lbm";
		case ".p7r":
			return "application/x-pkcs7-certreqresp";
		case ".wbmp":
			return "image/vnd.wap.wbmp";
		case ".dbx":
			return "application/x-dbx";
		case ".p7c":
		case ".p7m":
			return "application/pkcs7-mime";
		case ".img":
			return "application/x-img";
		case ".bot":
			return "application/x-bot";
		case ".ws":
		case ".ws2":
			return "application/x-ws";
		case ".ram":
		case ".rmm":
			return "audio/x-pn-realaudio";
		case ".sol":
		case ".sor":
		case ".txt":
			return "text/plain";
		case ".p7b":
		case ".spc":
			return "application/x-pkcs7-certificates";
		case ".png":
			return "image/png";
		case ".crl":
			return "application/pkix-crl";
		case ".cg4":
		case ".g4":
		case ".ig4":
			return "application/x-g4";
		case ".m4e":
		case ".mp4":
			return "video/mpeg4";
		case ".movie":
			return "video/x-sgi-movie";
		case ".mpeg":
		case ".mpg":
		case ".mpv":
			return "video/mpg";
		case ".ipa":
			return "application/vnd.iphone";
		case ".cal":
			return "application/x-cals";
		case ".drw":
			return "application/x-drw";
		case ".dbf":
			return "application/x-dbf";
		case ".pls":
		case ".xpl":
			return "audio/scpls";
		case ".dbm":
			return "application/x-dbm";
		case ".ssm":
			return "application/streamingmedia";
		case ".gbr":
			return "application/x-gbr";
		case ".907":
			return "drawing/907";
		case ".mpga":
			return "audio/rn-mpeg";
		case ".mfp":
		case ".swf":
			return "application/x-shockwave-flash";
		case ".hpg":
			return "application/x-hpgl";
		case ".rmvb":
			return "application/vnd.rn-realmedia-vbr";
		case ".rmf":
			return "application/vnd.adobe.rmf";
		case ".class":
		case ".java":
			return "java/*";
		case ".mi":
			return "application/x-mi";
		case ".xls":
			return "application/vnd.ms-excel";
		case ".pci":
			return "application/x-pci";
		case ".man":
			return "application/x-troff-man";
		case ".pcl":
			return "application/x-pcl";
		case ".mpd":
		case ".mpp":
		case ".mpt":
		case ".mpw":
		case ".mpx":
			return "application/vnd.ms-project";
		case ".xfdf":
			return "application/vnd.adobe.xfdf";
		case ".pcx":
			return "application/x-pcx";
		case ".edn":
			return "application/vnd.adobe.edn";
		case ".ptn":
			return "application/x-ptn";
		case ".iff":
			return "application/x-iff";
		case ".wri":
			return "application/x-wri";
		case ".smi":
		case ".smil":
			return "application/smil";
		case ".wrk":
			return "application/x-wrk";
		case ".sis":
		case ".sisx":
			return "application/vnd.symbian.install";
		case ".ai":
		case ".eps":
		case ".ps":
			return "application/postscript";
		case ".htc":
			return "text/x-component";
		case ".cmp":
			return "application/x-cmp";
		case ".p7s":
			return "application/pkcs7-signature";
		case ".xwd":
			return "application/x-xwd";
		case ".mac":
			return "application/x-mac";
		case ".cmx":
			return "application/x-cmx";
		case ".out":
			return "application/x-out";
		case ".hgl":
			return "application/x-hgl";
		case ".iii":
			return "application/x-iphone";
		case ".au":
		case ".snd":
			return "audio/basic";
		case ".smk":
			return "application/x-smk";
		case ".igs":
			return "application/x-igs";
		case ".ins":
		case ".isp":
			return "application/x-internet-signup";
		case ".epi":
			return "application/x-epi";
		case ".rt":
			return "text/vnd.rn-realtext";
		case ".pot":
		case ".ppa":
		case ".pps":
		case ".ppt":
		case ".pwz":
			return "application/vnd.ms-powerpoint";
		case ".nrf":
			return "application/x-nrf";
		case ".frm":
			return "application/x-frm";
		case ".rmx":
			return "application/vnd.rn-realsystem-rmx";
		case ".hqx":
			return "application/mac-binhex40";
		case ".mpa":
			return "video/x-mpg";
		case ".sld":
			return "application/x-sld";
		case ".tif":
		case ".tiff":
			return "image/tiff";
		case ".sit":
			return "application/x-stuffit";
		case ".slb":
			return "application/x-slb";
		case ".mnd":
			return "audio/x-musicnet-download";
		case ".wmd":
			return "application/x-ms-wmd";
		case ".fif":
			return "application/fractals";
		case ".dib":
			return "application/x-dib";
		case ".wp6":
			return "application/x-wp6";
		case ".lmsff":
			return "audio/x-la-lms";
		case ".cat":
			return "application/vnd.ms-pki.seccat";
		case ".wmz":
			return "application/x-ms-wmz";
		case ".cgm":
			return "application/x-cgm";
		case ".icb":
			return "application/x-icb";
		case ".rm":
			return "application/vnd.rn-realmedia";
		case ".rmj":
			return "application/vnd.rn-realsystem-rmj";
		case ".pdx":
			return "application/vnd.adobe.pdx";
		case ".red":
			return "application/x-red";
		case ".xdp":
			return "application/vnd.adobe.xdp";
		case ".wax":
			return "audio/x-ms-wax";
		case ".IVF":
			return "video/x-ivf";
		case ".x_b":
			return "application/x-x_b";
		case ".asf":
		case ".asx":
			return "video/x-ms-asf";
		case ".mid":
		case ".midi":
		case ".rmi":
			return "audio/mid";
		case ".c4t":
			return "application/x-c4t";
		case ".xfd":
			return "application/vnd.adobe.xfd";
		case ".wmv":
			return "video/x-ms-wmv";
		case ".hpl":
			return "application/x-hpl";
		case ".gp4":
			return "application/x-gp4";
		case ".wmx":
			return "video/x-ms-wmx";
		case ".wml":
			return "text/vnd.wap.wml";
		case ".cer":
		case ".crt":
		case ".der":
			return "application/x-x509-ca-cert";
		case ".ppm":
			return "application/x-ppm";
		case ".x_t":
			return "application/x-x_t";
		case ".ra":
			return "audio/vnd.rn-realaudio";
		case ".rmp":
			return "application/vnd.rn-rn_music_package";
		case ".pr":
			return "application/x-pr";
		case ".rec":
			return "application/vnd.rn-recording";
		case ".wr1":
			return "application/x-wr1";
		case ".dll":
		case ".exe":
			return "application/x-msdownload";
		case ".jfif":
		case ".jpe":
		case ".jpeg":
		case ".jpg":
			return "image/jpeg";
		case ".c90":
			return "application/x-c90";
		case ".ico":
			return "image/x-icon";
		case ".rjs":
			return "application/vnd.rn-realsystem-rjs";
		case ".awf":
			return "application/vnd.adobe.workflow";
		case ".emf":
			return "application/x-emf";
		case ".rjt":
			return "application/vnd.rn-realsystem-rjt";
		case ".wb2":
			return "application/x-wb2";
		case ".wb1":
			return "application/x-wb1";
		case ".wb3":
			return "application/x-wb3";
		case ".cit":
			return "application/x-cit";
		case ".rgb":
			return "application/x-rgb";
		case ".rms":
			return "application/vnd.rn-realmedia-secure";
		case ".stl":
			return "application/vnd.ms-pki.stl";
		case ".prf":
			return "application/pics-rules";
		case ".uin":
			return "application/x-icq";
		case ".wq1":
			return "application/x-wq1";
		case ".acp":
			return "audio/x-mei-aac";
		case ".hrf":
			return "application/x-hrf";
		case ".rpm":
			return "audio/x-pn-realaudio-plugin";
		case ".vcf":
			return "text/x-vcard";
		case ".lavs":
			return "audio/x-liquid-secure";
		case ".pc5":
			return "application/x-pc5";
		case ".apk":
			return "application/vnd.android.package-archive";
		case ".odc":
			return "text/x-ms-odc";
		case ".spl":
			return "application/futuresplash";
		case ".wpd":
			return "application/x-wpd";
		case ".wpg":
			return "application/x-wpg";
		case ".prn":
			return "application/x-prn";
		case ".prt":
			return "application/x-prt";
		case ".fax":
			return "image/fax";
		default:
			return "application/octet-stream";
		}
	}

}
