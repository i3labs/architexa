<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-beg.php') ?>
<title>Get Started with Architexa | Architexa</title>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/header-end.php') ?>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-header-learn-more.php') ?>
<script src="http://code.jquery.com/jquery-latest.min.js"></script> 

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js"></script>
<script type="text/javascript">
$( function() {
	   $('.silentClick').click(function(event){
			$.ajax({
				url: event.target.href,
				cache: false,
				success: function(html) {
					location.reload(true);
				}
			});
			return false;
	   });
});


function validate()
{
    var p = document.getElementById('form');
 
    var JSONObject = new Object;
    JSONObject.name = p['name'].value;
    JSONObject.email = p['email'].value;
    JSONObject.password = p['password'].value;
    JSONObject.re_password =""; // p['re_password'].value;
 /*   
if (p['inviteCodeName']!=null)
	JSONObject.inviteCodeName = "";//p['inviteCodeName'].value;
   */

    JSONObject.country = "";// p['country'].value;
    JSONObject.state = "";// p['state'].value;
    JSONObject.state2 = "";// p['state2'].value;

   
    JSONObject.feedbackChoice = new String;
 
   /*
    for(var i=0; i<5; i++)
    {
	if (p['feedbackChoice'][i].checked) {
        	    JSONObject.feedbackChoice = JSONObject.feedbackChoice + p['feedbackChoice'][i].value + ",";
	}
    }
    JSONObject.team = p['team'].value;
    JSONObject.problem = p['problem'].value;
    JSONObject.problem2 = p['problem2'].value;
*/


    JSONObject.referrer = p['referrer'].value; 
   JSONObject.subscribe = p['subscribe'].value;
 
    
    runAjax(JSONObject);

}



function runAjax(JSONObject){

	$.ajax({
		url: "http://my.architexa.com/users/createJSONP",
		dataType: "jsonp",
		cache: false,
 		jsonp: 'jsonp_callback',
		data: JSONObject,
		success: function(data) {
			if (data == null) {
				document.getElementById('msg').innerHTML = "There was an error with your request";
				document.getElementById('submitBtn').disabled=false;
				return;
			}
			if (data.status && data.status!='false') {
				location.href = "http://www.architexa.com/learn-more/install";
				return;
			}
			document.getElementById('msg').innerHTML = data.msg;
			document.getElementById('submitBtn').disabled=false;
		},
		type: "GET",

		error: function(XMLHttpRequest, textStatus, errorThrown) {
			document.getElementById('msg').innerHTML = errorThrown + " - " + textStatus;
			document.getElementById('submitBtn').disabled=false;
		}

	});
}

</script>





<? $code = $_GET["code"]; 
	if($code=="")
		$code = substr($_SERVER["REQUEST_URI"], 21);
?>


<h2 style="width:405px; text-align:left;">1. Register<a style="color:grey; font-weight:normal;" href="install">&nbsp&nbsp>>&nbsp 2. Download</a><a style="color:grey; font-weight:normal;" href="tour">&nbsp&nbsp>>&nbsp 3. Take a Tour</a></h2>

<br>


<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/quotes-inc.php') ?>

<style type="text/css"> 
<!--
	.p-input {
		width: 225px;
		margin-left: 5px;
		margin-bottom: 5px;
	}
	.p-input input {
		border-radius: 3px;
		box-shadow: 0 0 0 black, inset 0px 3px 3px #EEE;
		font-size: 16px;
		border: 1px solid #bfbfbf;
		padding: 5px;
		height: 24px;
		width: 225px;
	}
	.label {
		text-align: right;
		font-size: 16px;
	}
-->
</style>


<div style="float:left;width:400px">
	<div id="msg" style="color:red;font-weight:bold" >
	</div>

<!--<form id="form" action="http://localhost:8081/users" method="POST">-->
<form id="form" action="#" method="POST">
<font size="2" face="Verdana"> 
<table style="font-size:1em;">
<tr> 
<td colspan="2" id="response" style="color:red;font-weight:bold;"><?=($_GET["userFound"]) ? "E-mail Already Exists" : ""?></td> 
</tr> 

<!--<tr><td colspan="2" style="font-weight:bold; font-size:1.6em;">License Information</td><td></td></tr> 
<tr> 
	<td class="label">License Type <span style="color:red; font-size:.7em;" ></span></td> 
	<td><b>Free 30 day Trial</b></td> 
	<td></td> 
</tr>
-->
<tr><td colspan="2" style="font-weight:bold; font-size:1.6em;">Register for a Free license</td><td></td></tr> 
<tr><td colspan="3"><br/></td></tr>
<tr><td colspan="3">Sign up for an Architexa license. Free licenses are available in exchange for your feedback for teams of up to 3 people.</td></tr>
<tr><td colspan="3"><br/></td></tr>
<!--
<tr><td colspan="2" style="font-weight:bold; font-size:1.2em;">Account</td><td></td></tr> 
-->
<tr> 
	<td class="label">Name:</td> 
	<td>
		<div class="p-input">
		<input id="name" type="text" name="name" />
		</div>
	</td> 
	<td></td> 
</tr> 
<tr> 
	<td class="label">Password:</td> 
	<td>
		<div class="p-input">
		<input id="password" type="password" name="password" />
		</div>
	</td> 
	<td></td> 
</tr> 
<tr> 
	<td class="label">Email:</td> 
	<td>
		<div class="p-input">
		<input id="email" type="text" name="email" />
		</div>
	</td> 
	<td></td> 
</tr>
<tr> 
	<td colspan="2"><span style="font-style:italic">An email confirmation link will be sent to this address. Check your inbox before installing Architexa.</span></td> 
	<td></td> 
</tr> 
<!--
<tr> 
	<td class="label">Re-Enter Password</td> 
	<td><input id="re_password" style="width:225" type="password" name="re_password" /></td> 
	<td></td> 
</tr>  
-->
<? if ($code!="") { ?> 
<tr> 
	<td class="label">Invite Code <span style="font-style:italic;color:gray">(optional)</span></td> 
	<td><input type="text" style="width:225"name="inviteCodeName" value="<?=$code?>"/> 
</tr> 
<? } ?>
<script type="text/javascript"> 
 
function handleCountryChange() {
	if (document.getElementById('country').value != "USA") {
			document.getElementById('state').style.display= "none"; 
			document.getElementById('state').value= "";
	} else {
		document.getElementById('state').style.display= ""; 
	}
	if (document.getElementById('country').value != "Canada") {
		document.getElementById('Canada').style.display= "none"; 
		document.getElementById('Canada').value= "";
	} else {
		document.getElementById('Canada').style.display= ""; 
	}
}
function handleProblemChange() {
	if (document.getElementById('problem').value == "other") {
			document.getElementById('otherTR').style.display= ""; 
	} else {
		document.getElementById('otherTR').style.display= "none"; 
	}
}
</script> 
 
<tr><td colspan="3"><br/></td> 
<!--
<tr><td colspan="2" style="font-weight:bold; font-size:1.2em;">Optional</td><td></td></tr> 
<tr> 
	<td class="label">Location</td> 
	<td> 
		<select id="country" name="country" onChange="handleCountryChange()"> 
			<option value="">--Select Country--</option> 
<option value="USA" selected>United States</option> 
<option value="Canada">Canada</option> 
<option value="">----------------------------</option> 
<option value="Albania">Albania</option> 
<option value="Algeria">Algeria</option> 
<option value="American Samoa">American Samoa</option> 
<option value="Andorra">Andorra</option> 
<option value="Angola">Angola</option> 
<option value="Anguilla">Anguilla</option> 
<option value="Antigua">Antigua</option> 
<option value="Argentina">Argentina</option> 
<option value="Armenia">Armenia</option> 
<option value="Aruba">Aruba</option> 
<option value="Australia">Australia</option> 
<option value="Austria">Austria</option> 
<option value="Azerbaijan">Azerbaijan</option> 
<option value="Bahamas">Bahamas</option> 
<option value="Bahrain">Bahrain</option> 
<option value="Bangladesh">Bangladesh</option> 
<option value="Barbados">Barbados</option> 
<option value="Barbuda">Barbuda</option> 
<option value="Belgium">Belgium</option> 
<option value="Belize">Belize</option> 
<option value="Benin">Benin</option> 
<option value="Bermuda">Bermuda</option> 
<option value="Bhutan">Bhutan</option> 
<option value="Bolivia">Bolivia</option> 
<option value="Bonaire">Bonaire</option> 
<option value="Botswana">Botswana</option> 
<option value="Bosnia-Herzegovina">Bosnia&amp;Herzegovina</option> 
<option value="Brazil">Brazil</option> 
<option value="Virgin islands">British Virgin isl.</option> 
<option value="Brunei">Brunei</option> 
<option value="Bulgaria">Bulgaria</option> 
<option value="Burundi">Burundi</option> 
<option value="Cambodia">Cambodia</option> 
<option value="Cameroon">Cameroon</option> 
<option value="Cape Verde">Cape Verde</option> 
<option value="Cayman isl">Cayman Islands</option> 
<option value="Central African Rep">Central African Rep.</option> 
<option value="Chad">Chad</option> 
<option value="Channel isl">Channel Islands</option> 
<option value="Chile">Chile</option> 
<option value="China">China</option> 
<option value="Colombia">Colombia</option> 
<option value="Congo">Congo</option> 
<option value="cook isl">Cook Islands</option> 
<option value="Costa Rica">Costa Rica</option> 
<option value="Croatia">Croatia</option> 
<option value="Curacao">Curacao</option> 
<option value="Cyprus">Cyprus</option> 
<option value="Czech Republic">Czech Republic</option> 
<option value="Denmark">Denmark</option> 
<option value="Djibouti">Djibouti</option> 
<option value="Dominica">Dominica</option> 
<option value="Dominican Republic">Dominican Republic</option> 
<option value="Ecuador">Ecuador</option> 
<option value="Egypt">Egypt</option> 
<option value="El Salvador">El Salvador</option> 
<option value="Equatorial Guinea">Equatorial Guinea</option> 
<option value="Eritrea">Eritrea</option> 
<option value="Estonia">Estonia</option> 
<option value="Ethiopia">Ethiopia</option> 
<option value="Faeroe isl">Faeroe Islands</option> 
<option value="Fiji">Fiji</option> 
<option value="Finland">Finland</option> 
<option value="France">France</option> 
<option value="French Guiana">French Guiana</option> 
<option value="French Polynesia">French Polynesia</option> 
<option value="Gabon">Gabon</option> 
<option value="Gambia">Gambia</option> 
<option value="Georgia">Georgia</option> 
<option value="Germany">Germany</option> 
<option value="Ghana">Ghana</option> 
<option value="Gibraltar">Gibraltar</option> 
<option value="GB">Great Britain</option> 
<option value="Greece">Greece</option> 
<option value="Greenland">Greenland</option> 
<option value="Grenada">Grenada</option> 
<option value="Guadeloupe">Guadeloupe</option> 
<option value="Guam">Guam</option> 
<option value="Guatemala">Guatemala</option> 
<option value="Guinea">Guinea</option> 
<option value="Guinea Bissau">Guinea Bissau</option> 
<option value="Guyana">Guyana</option> 
<option value="Haiti">Haiti</option> 
<option value="Honduras">Honduras</option> 
<option value="Hong Kong">Hong Kong</option> 
<option value="Hungary">Hungary</option> 
<option value="Iceland">Iceland</option> 
<option value="India">India</option> 
<option value="Indonesia">Indonesia</option> 
<option value="Irak">Irak</option> 
<option value="Iran">Iran</option> 
<option value="Ireland">Ireland</option> 
<option value="Northern Ireland">Ireland, Northern</option> 
<option value="Israel">Israel</option> 
<option value="Italy">Italy</option> 
<option value="Ivory Coast">Ivory Coast</option> 
<option value="Jamaica">Jamaica</option> 
<option value="Japan">Japan</option> 
<option value="Jordan">Jordan</option> 
<option value="Kazakhstan">Kazakhstan</option> 
<option value="Kenya">Kenya</option> 
<option value="Kuwait">Kuwait</option> 
<option value="Kyrgyzstan">Kyrgyzstan</option> 
<option value="Latvia">Latvia</option> 
<option value="Lebanon">Lebanon</option> 
<option value="Liberia">Liberia</option> 
<option value="Liechtenstein">Liechtenstein</option> 
<option value="Lithuania">Lithuania</option> 
<option value="Luxembourg">Luxembourg</option> 
<option value="Macau">Macau</option> 
<option value="Macedonia">Macedonia</option> 
<option value="Madagascar">Madagascar</option> 
<option value="Malawi">Malawi</option> 
<option value="Malaysia">Malaysia</option> 
<option value="Maldives">Maldives</option> 
<option value="Mali">Mali</option> 
<option value="Malta">Malta</option> 
<option value="Marshall isl">Marshall Islands</option> 
<option value="Martinique">Martinique</option> 
<option value="Mauritania">Mauritania</option> 
<option value="Mauritius">Mauritius</option> 
<option value="Mexico">Mexico</option> 
<option value="Micronesia">Micronesia</option> 
<option value="Moldova">Moldova</option> 
<option value="Monaco">Monaco</option> 
<option value="Mongolia">Mongolia</option> 
<option value="Montserrat">Montserrat</option> 
<option value="Morocco">Morocco</option> 
<option value="Mozambique">Mozambique</option> 
<option value="Myanmar">Myanmar/Burma</option> 
<option value="Namibia">Namibia</option> 
<option value="Nepal">Nepal</option> 
<option value="Netherlands">Netherlands</option> 
<option value="Netherlands Antilles">Netherlands Antilles</option> 
<option value="New Caledonia">New Caledonia</option> 
<option value="New Zealand">New Zealand</option> 
<option value="Nicaragua">Nicaragua</option> 
<option value="Niger">Niger</option> 
<option value="Nigeria">Nigeria</option> 
<option value="Norway">Norway</option> 
<option value="Oman">Oman</option> 
<option value="Palau">Palau</option> 
<option value="Panama">Panama</option> 
<option value="Papua New Guinea">Papua New Guinea</option> 
<option value="Paraguay">Paraguay</option> 
<option value="Peru">Peru</option> 
<option value="Philippines">Philippines</option> 
<option value="Poland">Poland</option> 
<option value="Portugal">Portugal</option> 
<option value="Puerto Rico">Puerto Rico</option> 
<option value="Qatar">Qatar</option> 
<option value="Reunion">Reunion</option> 
<option value="Romania">Romania</option> 
<option value="Russia">Russia</option> 
<option value="Rwanda">Rwanda</option> 
<option value="Saba">Saba</option> 
<option value="Saipan">Saipan</option> 
<option value="Saudi Arabia">Saudi Arabia</option> 
<option value="Scotland">Scotland</option> 
<option value="Senegal">Senegal</option> 
<option value="Seychelles">Seychelles</option> 
<option value="Sierra Leone">Sierra Leone</option> 
<option value="Singapore">Singapore</option> 
<option value="Slovac Republic">Slovak Republic</option> 
<option value="Slovenia">Slovenia</option> 
<option value="South Africa">South Africa</option> 
<option value="South Korea">South Korea</option> 
<option value="Spain">Spain</option> 
<option value="Sri Lanka">Sri Lanka</option> 
<option value="Sudan">Sudan</option> 
<option value="Suriname">Suriname</option> 
<option value="Swaziland">Swaziland</option> 
<option value="Sweden">Sweden</option> 
<option value="Switzerland">Switzerland</option> 
<option value="Syria">Syria</option> 
<option value="Taiwan">Taiwan</option> 
<option value="Tanzania">Tanzania</option> 
<option value="Thailand">Thailand</option> 
<option value="Togo">Togo</option> 
<option value="Trinidad-Tobago">Trinidad-Tobago</option> 
<option value="Tunesia">Tunisia</option> 
<option value="Turkey">Turkey</option> 
<option value="Turkmenistan">Turkmenistan</option> 
<option value="United Arab Emirates">United Arab Emirates</option> 
<option value="U.S. Virgin isl">U.S. Virgin Islands</option> 
<option value="Uganda">Uganda</option> 
<option value="United Kingdom">United Kingdom</option> 
<option value="Urugay">Uruguay</option> 
<option value="Uzbekistan">Uzbekistan</option> 
<option value="Vanuatu">Vanuatu</option> 
<option value="Vatican City">Vatican City</option> 
<option value="Venezuela">Venezuela</option> 
<option value="Vietnam">Vietnam</option> 
<option value="Wales">Wales</option> 
<option value="Yemen">Yemen</option> 
<option value="Zaire">Zaire</option> 
<option value="Zambia">Zambia</option> 
<option value="Zimbabwe">Zimbabwe</option> 
	
		</select> 
		<select style="width: 50px; margin-top: 0px;" id="state" name="state"> 
	<option value=""> 
	</option><option value="AL">AL
	</option><option value="AK">AK
	</option><option value="AZ">AZ
	</option><option value="AR">AR
	</option><option value="CA">CA
	</option><option value="CO">CO
	</option><option value="CT">CT
	</option><option value="DE">DE
	</option><option value="DC">DC
	</option><option value="FL">FL
	</option><option value="GA">GA
	</option><option value="HI">HI
	</option><option value="ID">ID
	</option><option value="IL">IL
	</option><option value="IN">IN
	</option><option value="IA">IA
	</option><option value="KS">KS
	</option><option value="KY">KY
	</option><option value="LA">LA
	</option><option value="ME">ME
	</option><option value="MD">MD
	</option><option value="MA">MA
	</option><option value="MI">MI
	</option><option value="MN">MN
	</option><option value="MS">MS
	</option><option value="MO">MO
	</option><option value="MT">MT
	</option><option value="NE">NE
	</option><option value="NV">NV
	</option><option value="NH">NH
	</option><option value="NJ">NJ
	</option><option value="NM">NM
	</option><option value="NY">NY
	</option><option value="NC">NC
	</option><option value="ND">ND
	</option><option value="OH">OH
	</option><option value="OK">OK
	</option><option value="OR">OR
	</option><option value="PA">PA
	</option><option value="RI">RI
	</option><option value="SC">SC
	</option><option value="SD">SD
	</option><option value="TN">TN
	</option><option value="TX">TX
	</option><option value="UT">UT
	</option><option value="VT">VT
	</option><option value="VA">VA
	</option><option value="WA">WA
	</option><option value="WV">WV
	</option><option value="WI">WI
	</option><option value="WY">WY
	</option><option value="AA">AA&nbsp;
	</option><option value="AP">AP&nbsp;
	</option><option value="AE">AE&nbsp;
	</option></select> 
		<select id="Canada" name="state2" tabindex="10" class="form_dropdown" style="display:none;"> 
  <option value="" selected></option> 
  <option value="AB">Alberta</option> 
  <option value="BC">British Columbia</option> 
  <option value="MB">Manitoba</option> 
  <option value="NB">New Brunswick</option> 
  <option value="NF">New Foundland</option> 
  <option value="NT">Northwest Territories</option> 
  <option value="NS">Nova Scotia</option> 
  <option value="ON">Ontario</option> 
  <option value="PI">Prince Edward Island</option> 
  <option value="PQ">Quebec</option> 
  <option value="SA">Saskatchewan</option> 
  <option value="YT">Yukon Territory</option> 
</select> 
	</td> 
	<td></td> 
</tr> 
<tr> 
	<td class="label" >Development Team Size</td> 
	<td colspan="2"> 
		<select style="width:210px;" name="team"> 
			<option value="">--Select one--</option> 
			<option value="1-5">1 - 5</option> 
			<option value="6 - 25">6 - 25</option> 
			<option value="26 - 99">26 - 99</option> 
			<option value="100+">100+</option> 
		</select> 
	</td><td></td> 
</tr> 
 
<tr><td colspan="3"><br/></td> 
<tr><td colspan="2">We want to build the best tool for developers<br>how would you like to help?</td><td></td></tr> 
<tr> 
<td colspan="2"> 
 <table style="font-size:1em;"> 
  <tr> 
   <td><input type="checkbox" name="feedbackChoice" value="feedback sessions in Boston" /></td> 
   <td><label>I can meet for feedback sessions in Boston</label></td> 
  </tr> 
  <tr> 
   <td><input type="checkbox" name="feedbackChoice" value="interview via phone/IM" /></td> 
   <td><label>I can do a short interview via phone/IM</label></td> 
  </tr> 
  <tr> 
   <td><input type="checkbox" name="feedbackChoice" value=" screen sharing" /></td> 
   <td><label>I can do a short screen sharing session</label></td> 
  </tr> 
  <tr> 
   <td><input type="checkbox" name="feedbackChoice" value=" surveys" /></td> 
   <td><label>I'll participate in online surveys or user testing</label></td> 
  </tr> 
  <tr> 
   <td> <input type="checkbox" name="feedbackChoice" value=" experimental features" /></td> 
   <td><label>I'll give feedback on experimental features</label></td> 
  </tr>  
 </table> 
</td> 
<td rowspan="10">
 <div style="position:absolute; top:571px; left:696px;">
  <h3>Recent Press</h3>
  <blockquote style="font-style: italic; width: 300px;">"The difference with all the [Architexa] tools is that they are designed to 'surf' the source code through UML diagrams, so that one may be viewing more or less detail as you need. It is not an all or nothing, which is usually the case with traditional UML tools."</blockquote><span style="font-weight:bold">Abraham, JavaHispano</span><br>
  <blockquote style="font-style: italic; width: 300px;">"sometimes it can be too much work to go back and update diagrams once the coding process is underway, especially when under a tight deadline"</blockquote><span style="font-weight:bold">James Sugrue, JavaLobby</span>
 </div>
</td>
</tr> 
<tr><td colspan="3"><br/></td> 
<tr> 
	<td colspan="2">What problem would you like Architexa to solve?</td><td></td> 
</tr> 
<tr> 
	<td colspan="2"> 
		<select onchange="handleProblemChange()" style="width:325px" id="problem" name="problem"> 
			<option value="">--Select one--</option> 
			<option value="Getting up to speed faster">Getting up to speed faster</option> 
			<option value="managing">Managing large code bases</option> 
			<option value="Developer efficiency">Developer efficiency</option> 
			<option value="Easily documenting code">Easily documenting code</option> 
			<option value="Improving code quality">Improving code quality</option> 
			<option value="Preventing code deterioration">Preventing code deterioration</option> 
			<option value="Discussing/Communicating code concepts with team members">Discussing/Communicating code concepts with team members</option> 
			<option value="Testing and/or debugging code">Testing and/or debugging code</option> 
			<option value="other">Other</option> 
		</select> 
	</td><td></td> 
</tr> 
<tr id="otherTR" style="display:none;"> 
	<td colspan="3">Other: <input style="width:277" type="TEXT" name="problem2" /></td> 
	
</tr> 
-->
  <tr> 
   <td colspan=3><input type="checkbox" name="subscribe" value="1" checked/><label>Sign me up for the Architexa Newsletter!</label></td> 
  </tr> 

<tr><td colspan="3"><br/></td> 

<tr><td colspan="3" align="right">
<input type="hidden" name="referrer" value="<?=$referer?>" />
<input id="submitBtn" style="height:30px;" onclick="this.disabled=true;validate();" type="button" value="Proceed to Download" /></td></tr> 
</table>  
</FONT> 
</form> 
</div>

<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/side-menu-footer.php') ?>
<?php include($_SERVER['DOCUMENT_ROOT'].'/includes/footer2.php') ?>
