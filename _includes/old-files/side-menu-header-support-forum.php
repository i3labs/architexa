<?php 
function strEnd($haystack,$needle) {
  $expectedPosition = strlen($haystack) - strlen($needle);
  return strripos($haystack, $needle, 0) === $expectedPosition;
}
/*
 * topMenu: based on uri to output one of two things:
 * if selected:     <li><span>Learn More</span></li>
 * if not selected: <li><a href="/learn-more/index">Learn More</a></li>
 */
function topMenu($tgtUri, $tgtUriEx, $lbl) {
	$uri = $_SERVER['REQUEST_URI'];
	if (strBeg($uri,$tgtUri)) {
		echo "<li><span>" . $lbl . "</span></li>";
	} else {
		echo "<li><a href='" . $tgtUri . $tgtUriEx . "'>" . $lbl . 
				"</a><//li>";
	}
}
/*
 * topMenuStrict: Same as top menu but much more strict - it doesn't support 
 * nesting and is only shown when the exact match happens, such as with the 
 * homepage
 */
function topMenuStrict($tgtUri, $lbl) {
	$uri = $_SERVER['REQUEST_URI'];
	if (strEq($uri,$tgtUri)) {
		echo "<li><span>" . $lbl . "</span></li>";
	} else {
		echo "<li><a href='" . $tgtUri . "'>" . $lbl . 
				"</a><//li>";
	}
}
/*
 * sideMenu: based on uri to output one of two things:
 * if selected:     <div id='selected'>Company Info</div>
 * if not selected: <a href='/about/index'>Company Info</a>
 */
function sideMenu($baseUri, $tgtUri, $lbl) {
	$uri = $_SERVER['REQUEST_URI'];
	if (strEnd($uri,$tgtUri)) {
		echo "<div id='selected'>" . $lbl . "</div>";
	} else {
		echo "<a href='" . $baseUri . $tgtUri . "'>" . $lbl . "</a>";
	}
}
?>

<div id="body-left">


<div id="subMenuLeft2" style="padding-bottom:20px; padding-top:20px; border-bottom:1px dotted #ccc;">


<ul style="list-style-type:none;padding-left:35px"><li>
	<?php sideMenu("/support/", "index", "Overview") ?>
</li>



<li style="margin-top:10px">

	<?php sideMenu("/support/", "faq", "FAQ") ?>

</li><li style="margin-top:10px">
	<?php sideMenu("/support/", "webinar", "Webinar") ?>

</li>


<!--
<li style="margin-top:10px">

<?php if (strbeg($scriptName, "/user-guide")) { ?>
	User Guide
<?php } else { ?>
	<a href="/user-guide">User Guide</a>
<?php } ?>

</li>
-->

<!--
<li style="margin-top:10px">
	<a href="mailto:support@architexa.com">Email Us</a>
</li>
-->
</ul>
</div> <!-- end submenu -->

<!-- SERVICES-->


<div id="subMenuLeft2" style="margin-bottom:15px; margin-top:30px">


<ul style="list-style-type:none;padding-left:35px"><li>

<?php if (strbeg($scriptName, "/solutions/services")) { ?>
	<div id="selected">Services</div>
<?php } else { ?>
	<a href="/solutions/services">Services</a>
<?php } ?>

</li></ul>
</div> 




<!-- Videos -->
<div style="margin-top:15px;font-weight:bold">Videos</div>

<!--[if IE ]>
<div id="subMenuLeft2" style="margin-top:15px; margin-left: -40px">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="margin-top: 15px;">
<!--<![endif]-->

	<ul style="list-style-type:none;padding-left:35px">
	
		<li>
		<?php if (strbeg($scriptName, "/support/videos/intro/video")) { ?>
			<div id="selected">Introduction</div>
		<?php } else { ?>
			<a href="/support/videos/intro/video">Introduction</a>
		<?php } ?>
		</li>

		<li style="margin-top:10px">
		<?php if (strbeg($scriptName, "/support/videos/layered-diagrams/video")) { ?>
			<div id="selected">Layered Diagrams</div>
		<?php } else { ?>
			<a href="/support/videos/layered-diagrams/video">Layered Diagrams</a>
		<?php } ?>
		</li>

		<li style="margin-top:10px">
		<?php if (strbeg($scriptName, "/support/videos/class-diagrams/video")) { ?>
			<div id="selected">Class Diagrams</div>
		<?php } else { ?>
			<a href="/support/videos/class-diagrams/video">Class Diagrams</a>
		<?php } ?>
		</li>

		<li style="margin-top:10px">
		<?php if (strbeg($scriptName, "/support/videos/sequence-diagrams/video")) { ?>
			<div id="selected">Sequence Diagrams</div>
		<?php } else { ?>
			<a href="/support/videos/sequence-diagrams/video">Sequence Diagrams</a>
		<?php } ?>
		</li>

		</ul>

		<!--
		<?php if (strbeg($scriptName, "/support/videos/intro/video")) { ?>
			1. Introduction
		<?php } else { ?>
			<a href="/support/videos/intro/video">1. Introduction</a>
		<?php } ?>


		<div style="margin-top:10px">
		<?php if (strbeg($scriptName, "/support/videos/layered-diagrams/video")) { ?>
			2. Layered Diagrams
		<?php } else { ?>
			<a href="/support/videos/layered-diagrams/video">2. Layered Diagrams</a>
		<?php } ?>
		</div>

		<div style="margin-top:10px">
		<?php if (strbeg($scriptName, "/support/videos/class-diagrams/video")) { ?>
			3. Class Diagrams
		<?php } else { ?>
			<a href="/support/videos/class-diagrams/video">3. Class Diagrams</a>
		<?php } ?>
		</div>

		<div style="margin-top:10px">
		<?php if (strbeg($scriptName, "/support/videos/sequence-diagrams/video")) { ?>
			4. Sequence Diagrams
		<?php } else { ?>
			<a href="/support/videos/sequence-diagrams/video">4. Sequence Diagrams</a>
		<?php } ?>
		</div>
		-->
</div> <!-- end sub-menu -->


</div> <!-- end float -->



