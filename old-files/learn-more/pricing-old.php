<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Pricing Plan | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-learn-more-beg.php') ?>


<style type="text/css">
<!--

.leftTop {
	float:left;
	width:290px;
}
.rightTop {
	float:right;
	width:285px;
	padding:3px;
	background-color:#F0F0F0;
}
.tick {
	list-style-image: url(/images/check.gif);
	padding: 0px 0px 0px 30px;
}

.short {
	position: relative;
}
.short .head {
	text-decoration: none;
}

.tableBox {
	background-color: white;
	border: 2px solid #CCC;
	padding: 10px;
}
.short .body {
	position: absolute;
	top: 15px;
	left: -15px;
	width: 250px;
	text-align: center;
	z-index: 2;
	padding: 10px;
	border: 1px solid #a0a0a0;
	background: #ffffff;
	display: none;
}

#body-contents li {
	margin-left:0px;
}

#body-contents h3 {
	font-variant: normal;
}

.planName {
	font-size: 2.5em;
	font-weight:bold;
	font-style: italic;
	padding: 10px 10px 20px 0px;
	color:#cc6600;
}
.planText {
	padding: 10px;
}

.finePrintDiv {
	border-top: 1px solid black;
	font-size: 0.8em;
	margin-top: 30px;
	padding-top: 10px;
}
.fineIcon {
	float:left;
	padding:13px;
	vertical-align:middle;
}
-->
.floatTab {
	float:right;
	
}
</style>

<h1 style="font-size:34px;">Start making sense of your code now!</h1>

<span style="float:right;" id="showFloat" ><a id="showFloatBtn" href="#">[Floating Licenses]</a></span>
<span style="display:none; float:right;" id="hideFloat" ><a id="hideFloatBtn" href="#">[Per-user Licenses]</a></span>
<div style="clear:both"></div>

<div class="tableBox" style="height:350px;">
<div class="leftTop">
<table>
<tr valign="top">
  <td style="text-align:center">
<span class="planName" >Standard Edition</span><br><br><span id="basicPrice" style="padding-right:10px; color:black; font-size: 1.6em;font-style:italic;">$249/year</span><br><br></td>
</tr>

<tr>
 <td style="text-align:center">
 <a href = "/start/index"> 
<img style = "margin-right: 8px;" src ="/images/getStarted.png" width="200" border="0"/> 
</a> 
 </td>
</tr>

<tr>
<td>
<!--<div style="margin-top:5px;font-size:0.85em">We are giving a <b>large</b> discount to the first few users who signup and send us feedback. <span style="color:red">Signup now to get $112 off (*).</span><br><br> (*) - discount valid before August 16th.</div>-->
</td>
</tr>

<tr>
<td>
<p class="planText">
Take advantage of our powerful, interactive, and intuitive Rich Source Exploration (RSE) engine. Easily understand, quickly document, and discuss code quickly.</p>
</td>
</tr>

</table>

</div>


<div class="rightTop">
<h3>Powerful Diagramming Tools</h3>
<ul class="tick"> 
	<li class="short">
		<span class="head">Class Diagram Editor</span>
		<div class="body">See <b>code structure</b> easily by seeing inheritance, method calls, and other code relationships all in a single place using <b>Class Diagrams</b>.</div>
	</li> 
	<li class="short">
		<span class="head">Sequence Diagram Editor</span>
		<div class="body">Understand <b>complex code</b> quickly by seeing code interactions created in a single click and shown using <b>Sequence Diagrams</b>.</div>
	</li> 
	<li class="short">
		<span class="head">Layered Architectural Diagram Editor</span>
		<div class="body">Get a <b>high level view</b> of your codebase in seconds using automatically created <b>Layered Architectural Diagrams</b>.</div>
	</li> 
</ul>
<h3>Deep Eclipse Integration</h3>
<ul class="tick">
	<li class="short">
		<span class="head">Open Diagrams from Tabs</span>
		<div class="body"></div>
	</li> 

	<li class="short">
		<span class="head">Open stack trace in diagram</span>
		<div class="body"></div>
	</li> 
	<li class="short">
		<span class="head">Architexa Perspective</span>
		<div class="body"></div>
	</li> 
	<li class="short">
		<span class="head">Diagram related views</span>
		<div class="body"></div>
	</li> 
	<li class="short">
		<span class="head">Sophisticated real-time code analysis</span>
		<div class="body"></div>
	</li> 
	<li class="short">
		<span class="head">Up to date views of your code</span>
		<div class="body"></div>
	</li> 
</ul>
<h3>Collaboration Tools</h3>
<ul class="tick">

	<li class="short">
		<span class="head">Easily share diagrams</span>
		<div class="body"></div>
	</li> 

	<li class="short">
		<span class="head">Annotate and comment on diagrams</span>
		<div class="body"></div>
	</li> 

</ul> 

</div>
</div>
<div style="clear:both;"><br></div>


<span id="showProf" ><a id="showProfBtn" href="#">(+)</a> Advanced collaboration license options</span>
<span style="display:none" id="hideProf" ><a id="hideProfBtn" href="#">(-)</a> Advanced collaboration license options</span>

<div id="prof" style="display:none"> 
<div class="tableBox" style="height:175px;">
<div class="leftTop">
<table>
<tr valign="top">
  <td style="text-align:center">
<span class="planName" style="font-size: 1.8em;">Professional Edition</span><br><br><span id="profPrice" style="padding-right:10px; color:black; font-size: 1.6em;font-style:italic;">$449/year</span><br><br></td>
</tr>

<tr>
 <td style="text-align:center">
 <a href = "/start/index"> 
<img style = "margin-right: 8px;" src = "/images/getStarted.png" width="200" border="0"/> 
</a> 
 </td>
</tr>
<!-- 
<tr>
<td>
<p class="planText">
The Professional Edition comes with a version of our server that can be  used internally to share and collaborate without requiring a connection to the cloud.
</p>
</td>
</tr>
-->
</table>

</div>


<div class="rightTop" style="background-color:#FFF;">
<h3>*All the features of the Standard Edition</h3>
<h3>Additional Collaboration Tools</h3>
<ul class="tick">
	<li class="short">
		<span class="head">Dedicated local server to host diagrams inside your company network.</span>
		<div class="body"></div>
	</li> 
</ul> 

</div>
</div>

</div> <!-- end profesional -->


<script>
    
    $("#showProfBtn").click(function () {
      $("#prof").show("fast");
      $("#showProf").hide();
      $("#hideProf").show("fast");

      return true;
    });
    $("#hideProfBtn").click(function () {
        $("#prof").hide("fast");
        $("#showProf").show();
        $("#hideProf").hide("fast");
        return true;
      });

	</script>

<script>
    
    $("#showFloatBtn").click(function () {
      $("#basicPrice").html('$499/year');
      $("#profPrice").html('$899/year');
      $("#showFloat").hide();
      $("#hideFloat").show();

      return true;
    });
    $("#hideFloatBtn").click(function () {
        $("#basicPrice").html('$249/year');
        $("#profPrice").html('$449/year');
        $("#showFloat").show();
        $("#hideFloat").hide();
        return true;
      });

	</script>





<div class="finePrintDiv">

<div style="float:left;"><img class="fineIcon" src="/images/berlin/32x32/cost.png"/><p>We want to make sure that you like Architexa's suite, so all users receive a <b>free 30 day trial account - no credit card info required.</b></p></div>
<div style="clear:both;"></div>

<div style="float:left;"><img class="fineIcon" src="/images/berlin/32x32/finished-work.png"/><p class="finePrint">Once signed up for a trial account, you can upgrade to a paid license. Each paid license is renewable yearly, includes online support, and gives increased priority for bug fixes and feature requests. It can be cancelled at any time.</p></div>
<div style="clear:both;"></div>

<div style="float:left;"><img class="fineIcon" src="/images/berlin/32x32/hire-me.png"/><p class="finePrint">Each Architexa License is assigned to a specific user. This allows licensed users to enable their account on any computer with an installed Architexa client. Licenses can also be redistributed among your team.</p></div>
<div style="clear:both;"></div>

<div style="float:left;"><img class="fineIcon" src="/images/berlin/32x32/freelance.png"/><p class="finePrint">This release of Architexa Suite has been designed to work on Java projects, 150,000 lines of code, and teams of 4 members. Need some features? Send us your <a href="mailto:feedback@architexa.com">feedback</a>. As we hear your needs, we will be adding capabilities to the suite - updates will be made 3-4 times per year.</p></div>
<div style="clear:both;"></div>

<div style="float:left;"><img class="fineIcon" src="/images/berlin/32x32/consulting.png"/><p class="finePrint">We are also working with clients for private collaboration servers and enterprise Java (Struts, Tiles, etc). For access to them or for priority support please contact <a href="mailto:sales@architexa.com">sales</a>.</p></div>
<div style="clear:both;"></div>

</div>
</div>
<div style="clear:both"></div>
<div style="background:#cccccc; color:#333333; margin-top:5px; padding-top:5px; padding-bottom:5px; padding-left:5px; border:1px solid #999999;">

Make sense of your code now!
<a href="/start/index">
<b>Get Started </b></a>


</div>
</div>

<div style="padding:10px 0px; height:50px;">
<div style="float:left; font-size:smaller;">Copyright &copy;2011 Architexa, Inc. All rights reserved.<br/>
<a href="/privacy">Privacy Policy</a> 
</div>
<div valign="middle" style="float:right; vertical-align:middle; font-weight:bold">
<table valign="middle">
<tr valign="middle">
 <td>Follow Architexa</td>
 <td><a class="image" href="http://feeds.feedburner.com/Architexa"><img width="20" src="http://blog.architexa.com/wp-content/themes/simplex/images/rss2.png"/></a></td>
 <td><a class="image" href="http://www.facebook.com/Architexa"><img width="20" src="http://blog.architexa.com/wp-content/themes/simplex/images/facebook2.png"/></a></td>
 <td><a class="image" href="http://twitter.com/Architexa"><img width="20" src="http://blog.architexa.com/wp-content/themes/simplex/images/twitter2.png"/></a></td>

</tr>
</table>
</div> 
</div>


<script src="http://static.getclicky.com/js" type="text/javascript"></script>
<script type="text/javascript">try{ clicky.init(213593); }catch(err){}</script>
<noscript><p><img alt="Clicky" width="1" height="1" src="http://in.getclicky.com/213593ns.gif" /></p></noscript>
</body>
</html>





<!--
     FILE ARCHIVED ON 2:53:45 Jul 20, 2011 AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON 15:45:30 Feb 20, 2013.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
-->
