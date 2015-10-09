<!--
Note: <b>Beta</b> - Please do not distribute this url.<br>
-->

<div style="color:#cccccc;white-space:nowrap;float:right;">

<!--
<a href="/">Home</a>
-->

<!-- menu -->

<!--
<?php //echo "X" . $_SERVER["PHP_NAME"] . "X" ?>
<?php //echo "X" . $_SERVER["SCRIPT_NAME"] . "X" ?>
<?php //echo "X" . $_SERVER["REQUEST_URI"] . "X" ?>
-->

<?php
$scriptName = $_SERVER["REQUEST_URI"];
function strbeg($string, $search) {
    return (strncmp($string, $search, strlen($search)) == 0);
}
?>

<?php if (strbeg($scriptName, "/learn-more")) { ?>
	| Learn More |
<?php } else { ?>
	| <a href="/learn-more/index">Learn More</a> |
<?php } ?>

<?php if (strbeg($scriptName, "/solutions")) { ?>
	Solutions |
<?php } else { ?>
	<a href="/solutions/index">Solutions</a> |
<?php } ?>

<?php if (strbeg($scriptName, "/support") || strbeg($scriptName, "/user-guide") || strbeg($scriptName, "/forums")) { ?>
	Support |
<?php } else { ?>
	<a href="/support/index">Support</a> |
<?php } ?>

<?php if (strbeg($scriptName, "/about")) { ?>
	About Us
<?php } else { ?>
	<a href="/about/index">About Us</a>
<?php } ?>

</div>

