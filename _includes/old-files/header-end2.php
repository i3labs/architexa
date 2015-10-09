	<link rel="shortcut icon" href="/favicon.ico">
	<style type="text/css">
	<!--
	/* class */
	.main a:link {color: #ffff00; text-decoration: none; }
	.main a:active {color: #0000ff; text-decoration: underline; }
	.main a:visited {color: #ffff00; text-decoration: none; }
	.main a:hover {color: #ff0000; text-decoration: none; }

	/* id */
	#content a:link {color: #cc6600; text-decoration: none; }
	#content a:active {color: #0000ff; text-decoration: underline; }
	#content a:visited {color: #cc6600; text-decoration: none; }
	#content a:hover {color: #ff0000; text-decoration: none; }

	a:link {color: #ffff00; text-decoration: none; }
	a:active {color: #0000ff; text-decoration: underline; }
	a:visited {color: #ffff00; text-decoration: none; }
	a:hover {color: #ff0000; text-decoration: none; }

	#menu a:link {color: #888888; text-decoration: none; }
	#menu a:active {color: #888888; text-decoration: none; }
	#menu a:visited {color: #888888; text-decoration: none; }
	#menu a:hover {color: #ff4444; text-decoration: none; }

	.main {
		color: #444444;
		text-align:left;
		margin:0 auto;
		width:850px;
		//font-family: Verdana, Arial, sans-serif;
		font-family:Calibri,Helvetica,Geneva,Arial,Verdana,sans-serif;
		//font-size: 0.75em;
		//line-height: 150%;
	}
	p {
		padding-top: 5px;
		margin-top: 5px;
		margin-bottom: 5px;
	}
	code {
		font-size: 1.2em;
	}
	#body-left {
		float:left;
		padding-left:20px;
	}
	
	/* Old Grey Box SubMenu */
	.subMenuLeft {
		background: #cccccc;
		color: white;
		width: 175px;
		margin-top: 25px;
		padding-top: 10px;
		padding-bottom: 10px;
	}
	
	/* New Blue Effect SubMenu */
	#subMenuLeft2 a {
		background: #EBF2F2; border-left: 4px solid #A1C1BE;
		text-decoration: none;
		display: block;
		width: 150px; margin: -6px 0 10px -10px; padding: 6px 1px 6px 10px; 
		}
		
	#subMenuLeft2 a:hover {
		background: #EBF7F8; border-left: 4px solid #333;
	}

	#subMenuLeft2 a:link {color: #c60; text-decoration: none;  }
	#subMenuLeft2 a:active {color: #c60; text-decoration: none;  }
	#subMenuLeft2 a:visited {color: #c60; text-decoration: bold; }
	#subMenuLeft2 a:hover {color: #333; text-decoration: bold; font-weight:bold; }
	
	#selected{
		background: #EBF7F8; border-left: 4px solid #333;
		font-weight: bold;
		width: 150px; margin: -6px 0 10px -10px; padding: 6px 1px 6px 10px; 
	}
	
	#nested a {
		background: url("../images/dbg.png") left no-repeat #fafafa; 
		border: none;
		width: 130px; margin: -8px 0 10px -15px; padding: 6px 1px 6px 20px; 
		font-size: smaller;
	}
	#nested a:hover {
		background: url("../images/dbg2hover.png") left no-repeat #EBF7F8; 
		border: none;
	}
	
	#nested-selected {
		background: url("../dbg2hover.png") left no-repeat #EBF7F8;
		border: none;
		font-weight: bold;
		width: 130px; margin: -8px 0 10px -15px; padding: 6px 1px 6px 20px;
	}
	
	/* body-left/subMenuLeft + body-contents = 800 ~ 850 total */
	#body-contents {
		float:right; 
		width:605px; 
		margin-left: 5px;
		padding-right: 20px;
		//margin-top: 6px;
	}
	.note {
		color: black;
		background-color: #CCCCCC;
		border: 1px solid #999999;

		padding-top: 2px;
		padding-bottom: 2px;
		padding-left: 5px;
		padding-right: 5px;
	}
	.floatImage {
		float:right;
		margin:15px;
		font-size: 80%;
		text-align: center;
	}
	.leftImage {
		margin:15px;
		font-size: 80%;
		text-align: left;
	}
	.calloutPara {
		padding: 5px;
		padding-bottom: 10px;
		margin-top:25px;
		font-size: 90%;
		width: 200px;
		background: #cccccc;
		border: 1px solid #999999;
	}
	.calloutPlus {
		margin: 10px 0px; 
		padding: 5px; 
		width:100%;
	}

	h1 {
		font-size: 165%;
		//margin-top: 25px;
	}
	h2 {
		font-size: 130%;
		margin: 5px 0 5px 0;
		line-height: 140%;
	}
	h3 {
		font-size: 100%;
		margin: 15px 0 0px 0;
		font-variant: small-caps;
	}
	h4 {
		font-size: 100%;
		font-weight: bold;
		margin: 15px 0 0px 0;
	}
	ul {
		margin-top: 3px;
		margin-bottom: 3px;
	}
	li {margin: 3px 0 3px -20px;}

	
	#lmContDiv {
		overflow: auto;
	}
	
	#lmImageAlign {
		float: right;
		margin-top : 10px;
	}

	#lmTextAlign {
		width : 60%;
	}
	
	hr {
		background-color : #CCCCCC;
		border : 0px;
	}
	-->
	</style>

<script type="text/javascript" src="/js/jquery-1.4.4.min.js"></script>
<script type="text/javascript"> 

function clh() {
var e = document.getElementById(location.hash);
 if (e != null) {
  e.style.backgroundColor = 'transparent';
 }
}

function h() {
var e = document.getElementById(location.hash);
 if (e != null) {
  e.style.backgroundColor = '#FFFF00';
 }
 setTimeout('clh()', 1500);
}

</script>

</head>
<!--
<body bgcolor="#00476B" style="padding:0px;margin:0px"> 
selected for the new color scheme -->
<!--lighter shade of grey: #FAFAFA-->
<body onload="h()" bgcolor="#F0F0F0" style="padding:0px;margin:0px;text-align:center">


<!--
<div style="margin:0 auto;width:900px;">
-->

<div class="topbar" style="padding:0px;width:100%;text-align:left;background-color:#00476B; border-bottom: 3px double #ffffff">
<div class="main">

<center>
<table border="0" width="100%">
<tr>
<td rowspan="2" width="425" valign=top>
<!-- banner -->
<a href="/" style="white-space:nowrap">
	<!--<img style="border:0" src="/images/logo-icon.png">-->
	<img alt="Architexa - Understand and Document" style="border:0;margin-left:-9px;margin-top:15px;margin-bottom:20px" src="/images/logo-main.png">
</a>
</td>
<td rowspan="2" >&nbsp;</td>
<td width="250" valign="top" align="right">
	<div id="menu" style="font-size:80%">
<!--
	Signup |
	Blog | 
	Forums | 
	Feedback
-->
<div style="color:#ffffff">
	
	<a href="http://my.architexa.com">Log In</a>
	|
	<a href="/start/index">Download</a>
	|
	<a href="http://blog.architexa.com/">Blog</a>
	|
	<a href="/about/contact">Contact Us</a>
</div>	

	</div>
</td></tr>
<tr>
<td width=400 valign=center>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/top-menu-header.php') ?>
</td>
</tr>
</table>
</center>
</div> <!-- main -->
</div> <!-- topbar -->

<div class="main" id="content">
<!-- Body with new color scheme: white -->
<div class="main-body" style="background-color:#fff; border-left: 1px solid #ccc; border-right: 1px solid #ccc; border-bottom: 1px solid #ccc; padding-bottom:20px;"> 


