<?php
$callbackMeth = $_GET['jsonp_callback'];
$json = <<<JSON
	{'status' : 'success'}
JSON;

// // return error
// $json = <<<JSON
// 	{'msg' : 'System Undergoing Maintenance'}
// JSON;


$zendLibraryPath = '../includes/lib/ZendFramework-1.12.7/library';
$clientLibraryPath = '../includes/lib/ZendGdata-1.12.7/library';
set_include_path(get_include_path() . 
    PATH_SEPARATOR . $zendLibraryPath .
    PATH_SEPARATOR . $clientLibraryPath
);


$email = 'vineet@architexa.com';
$pass = 'f1r3Ba!!';

// key for i3labs-tracking
$spreadsheetKey = '1_pJc3yJ7WBkN3C0Slz9uBNPbciOKZAsk-ChCGxlVLbA';
// first worksheet
$currWkshtId = 'od6';


require_once 'Zend/Loader.php';

Zend_Loader::loadClass('Zend_Gdata');
Zend_Loader::loadClass('Zend_Gdata_ClientLogin');
Zend_Loader::loadClass('Zend_Gdata_Spreadsheets');
Zend_Loader::loadClass('Zend_Gdata_App_AuthException');
Zend_Loader::loadClass('Zend_Http_Client');


$gdClient = Zend_Gdata_ClientLogin::getHttpClient(
                $email, 
                $pass,
                Zend_Gdata_Spreadsheets::AUTH_SERVICE_NAME
            );
$gSheet = new Zend_Gdata_Spreadsheets($gdClient);

$registrationRec = $_GET;
unset($registrationRec['jsonp_callback']);
unset($registrationRec['_']);

$gSheet->insertRow(
		$registrationRec,
        $spreadsheetKey,
        $currWkshtId
    );

echo "/*\n";
var_dump($_GET);
echo "*/\n";

/*
  ["jsonp_callback"]=>
  string(18) "jsonp1402864885732"
  ["_"]=>
  string(13) "1402864893273"
  ["name"]=>
  string(1) "a"
  ["email"]=>
  string(1) "a"
  ["password"]=>
  string(1) "a"
  ["re_password"]=>
  string(0) ""
  ["phone"]=>
  string(1) "a"
  ["title"]=>
  string(1) "a"
  ["company"]=>
  string(1) "a"
  ["website"]=>
  string(1) "a"
  ["planName"]=>
  string(9) "community"
  ["country"]=>
  string(0) ""
  ["state"]=>
  string(0) ""
  ["state2"]=>
  string(0) ""
  ["feedbackChoice"]=>
  string(0) ""
  ["referrer"]=>
  string(0) ""
  ["subscribe"]=>
  string(1) "1"
}
*/










echo "$callbackMeth($json);";
?>