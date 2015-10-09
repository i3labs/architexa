<?php
if (!isset($_COOKIE["referer"])) {
	$referer = $_SERVER['HTTP_REFERER'] .",". date("Y-m-d");;
	setcookie("referer",$referer, time()+60*60*24*365, "/", ".architexa.com");
} else {
	$referer = $_COOKIE["referer"];
}
?>
