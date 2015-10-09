<?php
$callbackMeth = $_GET['jsonp_callback'];
$json = <<<JSON
	{'status' : 'success'}
JSON;

$registrationRec = $_GET;
unset($registrationRec['jsonp_callback']);
unset($registrationRec['_']);

$registrationFile = "/home/architexaweb/architexa.com/learn-more/registration-data.txt";

file_put_contents($registrationFile, json_encode($registrationRec) . "\n", FILE_APPEND);

echo "$callbackMeth($json);";
?>