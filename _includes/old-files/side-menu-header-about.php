<div id="body-left">

<!--[if IE ]>
<div id="subMenuLeft2" style="margin-top:15px; padding-bottom:20px; border-bottom:1px dotted #ccc; margin-left: -40px;">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="margin-top:15px; padding-bottom:20px; border-bottom:1px dotted #ccc;">
<!--<![endif]-->


<ul style="list-style-type:none;padding-left:35px"><li>

<?php if (strbeg($scriptName, "/about/index")) { ?>
	<div id="selected">Company</div>
<?php } else { ?>
	<a href="/about/index">Company</a>
<?php } ?>

</li><li style="margin-top:10px">

<?php if (strbeg($scriptName, "/about/team")) { ?>
	<div id="selected">Team</div>
<?php } else { ?>
	<a href="/about/team">Team</a>
<?php } ?>

</li><li style="margin-top:10px">

<?php if (strbeg($scriptName, "/blog")) { ?>
	<div id="selected">News</div>
<?php } else { ?>
	<a href="/blog">News</a>
<?php } ?>

</li>

<li><div id="nested"><ul>
<li style="list-style-type: none;">
	<a href="/blog/category/updates">Updates</a>
</li>

<li style="list-style-type: none;">
	<a href="/blog/category/feedback">Feedback</a>
</li>

<li style="list-style-type: none;">
	<a href="/blog/category/events">Events</a>
</li>

<li style="list-style-type: none;">
	<a href="/blog/category/general">General</a>
</li>

</ul></div></li>




<li style="margin-top:10px">

<?php if (strbeg($scriptName, "/about/affiliation")) { ?>
	<div id="selected">Affiliations</div>
<?php } else { ?>
	<a href="/about/affiliation">Affilliations</a>
<?php } ?>

</li><li style="margin-top:10px">
<?php if (strbeg($scriptName, "/about/contact")) { ?>
	<div id="selected">Contact Us</div>
<?php } else { ?>
	<a href="/about/contact">Contact Us</a>
<?php } ?>

</li></ul>
</div> <!-- end sub-menu -->

<!-- Tech Menu-->

<!--[if IE ]>
<div id="subMenuLeft2" style="margin-top:15px; padding-top:15px; margin-left: -40px;">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="margin-top:15px; padding-top:15px;">
<!--<![endif]-->

<ul style="list-style-type:none;padding-left:35px">
<li>
<?php if (strbeg($scriptName, "/about/labs/index")) { ?>
	<div id="selected">Labs</div>
<?php } else { ?>
	<a href="/about/labs/index">Labs</a>
<?php } ?>
</li>

<li style="margin-top:10px">
<?php if (strbeg($scriptName, "/about/credits")) { ?>
	<div id="selected">Thanks!</div>
<?php } else { ?>
	<a href="/about/credits">Thanks!</a>
<?php } ?>
</li>

</ul>
</div> <!-- end Tech menu -->

<?php if (strbeg($scriptName, "/blog")) { ?>
<div style="margin-top:15px;font-weight:bold">Subscribe via Email:</div>
<form action="http://feedburner.google.com/fb/a/mailverify" method="post" target="popupwindow" onsubmit="window.open('http://feedburner.google.com/fb/a/mailverify?uri=ArchitexaNews', 'popupwindow', 'scrollbars=yes,width=550,height=520');return true"><input type="text" style="width:120px" name="email"/><input type="hidden" value="ArchitexaNews" name="uri"/><input type="hidden" name="loc" value="en_US"/><input type="submit" value="Subscribe" /></form>
<?php } else { ?>
<?php } ?>


<div style="margin-top:15px;font-weight:bold">Questions?</div>
<a href="/support">Architexa Support</a>

</div> <!-- end float -->

<!--[if IE ]>
<div id="body-contents-IE">
<![endif]-->
<!--[if !IE]>-->
<div id="body-contents">
<!--<![endif]-->


