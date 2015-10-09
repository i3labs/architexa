<div id="body-left">

<!--[if IE ]>
<div id="subMenuLeft2" style="padding-top:20px;padding-bottom:15px;border-bottom:1px dotted #ccc; margin-left: -40px;">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="padding-top:20px;padding-bottom:15px;border-bottom:1px dotted #ccc;">
<!--<![endif]-->

<ul style="list-style-type:none;padding-left:35px"><li>

<?php if (strbeg($scriptName, "/start/index")) { ?>
	<div id="selected">Register</div>
<?php } else { ?>
	<a href="/start/index">Register</a>
<?php } ?>

</li><li style="margin-top:10px">

<?php if (strbeg($scriptName, "/start/install")) { ?>
	<div id="selected">Installation Details</div>
<?php } else { ?>
	<a href="/start/install">Installation Details</a>
<?php } ?>
</li>
<li style="margin-top:10px">

<?php if (strbeg($scriptName, "/start/tour")) { ?>
	<div id="selected">Take a Tour</div>
<?php } else { ?>
	<a href="/start/tour">Take a Tour</a>
<?php } ?>
</li></ul>
</div> <!-- end sub-menu -->


<!--[if IE ]>
<div id="subMenuLeft2" style="margin-top:30px; margin-left: -40px;">
<![endif]-->
<!--[if !IE]>-->
<div id="subMenuLeft2" style="margin-top:30px;">
<!--<![endif]-->

<ul style="list-style-type:none;padding-left:35px">
<li style="margin-top:10px">

<?php if (strbeg($scriptName, "/start/pricing")) { ?>
	<div id="selected">Pricing</div>
<?php } else { ?>
	<a href="/start/pricing">Pricing</a>
<?php } ?>

</li><li style="margin-top:10px">

<?php if (strbeg($scriptName, "/start/open-source")) { ?>
	<div id="selected">Open Source Initiative</div>
<?php } else { ?>
	<a href="/start/open-source">Open Source Initiative</a>
<?php } ?>

</li>
<li style="margin-top:10px">

<?php if (strbeg($scriptName, "/start/collaboration")) { ?>
	<div id="selected">Collaboration</div>
<?php } else { ?>
	<a href="/start/collaboration">Collaboration</a>
<?php } ?>

</li>
</ul>
</div>

<div style="margin-top:15px;font-weight:bold">Questions?</div>
See <a href="/support">Architexa Support</a> or <br />call us at (617) 500-2596

</div> <!-- end float -->

<!--[if IE ]>
<div id="body-contents-IE">
<![endif]-->
<!--[if !IE]>-->
<div id="body-contents">
<!--<![endif]-->
