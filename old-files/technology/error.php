<?php

$page_redirected_from = $_SERVER['REQUEST_URI'];  // this is especially useful with error 404 to indicate the missing page.
$server_url = "http://" . $_SERVER["SERVER_NAME"] . "/";
$redirect_url = $_SERVER["REDIRECT_URL"];
$redirect_url_array = parse_url($redirect_url);
$end_of_path = strrchr($redirect_url_array["path"], "/");

switch(getenv("REDIRECT_STATUS"))
{
	# "400 - Bad Request"
	case 400:
	$error_code = "400 - Bad Request";
	$explanation = "The syntax of the URL submitted by your browser could not be understood.  Please verify the address and try again.";
	$redirect_to = "";
	break;

	# "401 - Unauthorized"
	case 401:
	$error_code = "401 - Unauthorized";
	$explanation = "This section requires a password or is otherwise protected.  If you feel you have reached this page in error, please return to the login page and try again, or contact the webmaster if you continue to have problems.";
	$redirect_to = "";
	break;

	# "403 - Forbidden"
	case 403:
	$error_code = "403 - Forbidden";
	$explanation = "This section requires a password or is otherwise protected.  If you feel you have reached this page in error, please return to the login page and try again, or contact the webmaster if you continue to have problems.";
	$redirect_to = "";
	break;

	# "404 - Not Found"
	case 404:
	$error_code = "404 - Not Found";
	$explanation = "The requested resource '" . $page_redirected_from . "' could not be found on this server.  Please verify the address and try again.";
	#$redirect_to = $server_url . "wiki" . $end_of_path;
	$redirect_to = $server_url;
	break;

	# "500 - Internal Server Error"
	case 500:
	$error_code = "500 - Internal Server Error";
	$explanation = "The server experienced an unexpected error.  Please verify the address and try again.";
	$redirect_to = "";
	break;
}
?>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>

<?php
	if (strcmp($redirect_to, "") != 0)
	{
?>

	<meta http-equiv="Refresh" content="5; url='<?php print($redirect_to); ?>'">
<?php
	}
?>

	<title>Page not found: <?php print ($redirect_to); ?></title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>

<h1>Error Code <?php print ($error_code); ?></h1>

<p>The <a href="http://en.wikipedia.org/wiki/Uniform_resource_locator">URL</a> you requested was not found. <?PHP echo($explanation); ?></p>

<p><strong>Did you mean to type <a href="<?php print ($redirect_to); ?>"><?php print ($redirect_to); ?></a>?</strong>  You will be automatically redirected there in five seconds.</p>

<p>You may also want to try starting from the home page: <a href="<?php print ($server_url); ?>"><?php print ($server_url); ?></a></p>

<hr />

<p><i>A project of <a href="<?php print ($server_url); ?>"><?php print ($server_url); ?></a>.</i></p>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer.php') ?>
