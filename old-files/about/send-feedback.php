<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head><title>Architexa Feedback</title></head>
<body>

<p>Thanks for the feedback!</p>

<?php
	//$pear_user_config = '/home/archiexaweb/.pearrc';
	set_include_path(
		get_include_path() . 
		PATH_SEPARATOR . '/home/architexaweb/pear/php'
	);
	require_once "Mail.php";
	require_once "Mail/mime.php";
	
	$agent = $_SERVER['HTTP_USER_AGENT'];
	$uri = $_SERVER['REQUEST_URI'];
	$ip = $_SERVER['REMOTE_ADDR'];
	$dtime = date('r');


	$fp = fopen("logs/feedback.txt", "a");

	// date time first
	fputs($fp, $dtime);

	// form info
	if ($_POST['comments'] == "") {
		fputs($fp, " | No Submission");
		echo "<br>No Content";
	} else {
		$email = $_POST['email'];
		fputs($fp, " | EMail: $email");

		$comments = $_POST['comments'];
		fputs($fp, " | Comments: $comments");

		$type = $_POST['feedbackType']; 
		fputs($fp, " | Feedback Type: $type");
		
		if ($email=="") $email = "notLoggedIn@architexa.com";
		// send e-mail
		$from = $email;
		$to = "feedback@architexa.com";
		$subj = 'Architexa Form Feedback';
		$body = "$agent\n-----------------\nFrom: $from\nType: $type\n$comments\n";

		$smtp = Mail::factory('smtp', array (
				'host' => "ssl://smtp.gmail.com",
				'port' => "465",
				'auth' => true,
				'username' => "web@architexa.com",
	   			'password' => "architexaBew"
		));
		
							
		$headers = array (
				'From' => $from, // google overrites this to be username
				'To' => $to,
				'Subject' => $subj);

		$mime = new Mail_mime();
		$mime->setTXTBody($body);
		//$mime->setHTMLBody("<html>" . $body . "</html>");
		
		$file = $_POST['errors'];                                    // Content of the file
        	$file_name = "errors.txt";                                   // Name of the Attachment
        	$content_type = "text/plain";                                // Content type of the file
        	$mime->addAttachment ($file, $content_type, $file_name, 0);  // Add the attachment to the email

		$file = $_POST['config'];                                    // Content of the file
        	$file_name = "config.txt";                                   // Name of the Attachment
        	$content_type = "text/plain";                                // Content type of the file
        	$mime->addAttachment ($file, $content_type, $file_name, 0);  // Add the attachment to the email

		$bodyM = $mime->get();
		$headers = $mime->headers($headers);
		
		//$mail = $smtp->send($to, $headers, $body);
		$mail = $smtp->send($to, $headers, $bodyM);
		if (PEAR::isError($mail)) {
			fputs($fp, " | MAIL ERROR: $mail->getMessage()");
			echo "<br><br><b>Error:</b> " . $mail->getMessage();
		}

	}

	// additional information
	$entry_line = " | IP: $ip | Agent: $agent  | URL: $uri \n";
	fputs($fp, $entry_line);


	fclose($fp);
?>

</body></html>

