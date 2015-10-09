<style type="text/css">
.date {
	font-style:italic;
	color:gray;
}
.quote {
	font-style: italic;
	margin: 39px 0px 12px 67px;
	padding-left: 20px;
	width: 70%;
}

.testContent {
	background: #FFF;
	border: 1px solid #E4E4E4;
	color: #616161;
	display: table-cell;
	padding: 12px;
	text-align: center;
	z-index: 9;
}
.testQuote{
	font-family: Georgia, Times, serif;
	font-size: 15px;
	padding: 0px;
}
.author{
	color: #9B9B9B;
	font-family: Georgia, Times, serif;
	font-size: 12px;
	font-style: italic;
	padding: 0px;
}
</style>


<?php
	$xmlfile = $_SERVER['DOCUMENT_ROOT'].'/includes/quotes.xml';
	$xmlparser = xml_parser_create();

	// open a file and read data
	$fp = fopen($xmlfile, 'r');
	$xmldata = fread($fp, 4096);
	xml_parse_into_struct($xmlparser,$xmldata,$values,$tags);
	xml_parser_free($xmlparser);

	//print_r($values);
	//print_r($tags);

// loop through the structures
    $count = 0;
    foreach ($tags as $key=>$val) {
        if ($key == "TEXT") {
		foreach($val as $index) {
			for ($i=0; $i<count($tags)*2;$i=$i+2) {
				if ($values[$index+$i][tag] == "QUOTES" || $values[$index+$i][tag] == "ITEM") continue;
				$item[$values[$index+$i][tag]] = $values[$index+$i][value];
				$items[$count] = $item;
			}
			$count++;
		}
        } else {
            continue;
        }
    }
    //print_r($items);
?>
<div style="text-align:center;">


		<div style="margin-left: auto; margin-right: auto; height:150px; width:400px; padding-top:18px;" align="center" id="quotes">
			<div id="testimonial-0" class="testContent"> 
				<span style="font-weight:bold; color:#444;font-size:1.2em;" class="testQuote">Featured Customers:</span><br> 		
				<a href="http://www.citi.com"><img style="height:40px" border=0 alt="citi" src="/images/quotes/citi.gif" /></a>
				<a href="https://www.fidelity.com/"><img style="width: 160px;" border=0 alt="fidelity" src="/images/quotes/fidelity-investments-or.png" /></a>
				<a href="http://www.nakinasystems.com/"><img style="width: 76px;" border=0 alt="nakina" src="/images/quotes/nakina.jpg" /></a>
				<a href="http://www.omgeo.com/"><img style="height: 58px;" border=0 alt="omego" src="/images/quotes/omego.jpg" /></a>		
				<a valign="middle" href="http://www.logiball.de/"><img valign="middle" border=0 alt="logiball" src="/images/quotes/logiball.gif" /></a>					
	
			</div>
			
			<div id="testimonial-1" class="testContent"> 
				<span style="font-weight:bold; color:#444;font-size:1.2em;" class="testQuote">Affiliations:</span><br> 		
				<a href="/about/affiliation"><img border="0" src="/images/eclipse_logo.png" style="height:45px"></a>
				<a href="/about/affiliation"><img border="0" src="/images/agile_logo.png" style="height:47px"></a>
				<a href="/about/affiliation"><img border="0" src="/images/IASA_logo.png"  style="height:47px"></a>
			</div>

			<?
				$i=2;
				foreach ($items as $item) {
			?>
			<div id="testimonial-<?=$i?>" class="testContent"> 
				<? if ($item['IMGLOC']!="") { ?>
				<a href="<?=$item['SOURCELINK']?>"><img  height="30" alt="<?=$item['SOURCE']?>" src="<?=$item['IMGLOC']?>" /></a><br>
				<? } ?>
				<span class="testQuote">"<?=$item['TEXT']?>"</span><br> 
				<span class="author">- <?=$item['NAME']?>, <a href="<?=$item['SOURCELINK']?>"><?=$item['SOURCE']?></a></span>
			</div>
			<?
				$i++;
				}
			?>
			
			


		</div>
		<span id="nav">
		 <img id="0" onClick="slideTo('testimonial-0')" src="/images/bullet_active.gif"/>
		<? for ($j=1;$j<count($items)+1;$j++) {?>
		 <img id="<?=$j?>" onClick="slideTo('testimonial-<?=$j?>')" src="/images/<?= ($j==0) ? 'bullet_active.gif': 'bullet.gif'?>"/>
		<? } ?>

		</span>
<script> 
function init() {
 $('#quotes div').hide();
 $('#quotes div:first').show();
}

function slide() {
    var $top = $('#quotes div:visible');
    var num = $top.attr('id').split('-').pop();
    var $topNav = $('#'+num);

    var $next;
    var $nextNav;
 if($top.next().length > 0) {
 	 $next = $top.next();
	 $nextNav = $topNav.next();
 } else {
       $next = $('#quotes div:first');
       $nextNav = $('#nav img:first');
 }
  $top.fadeOut("slow",function() {
   $next.fadeIn("slow");

  });
 
 $topNav.attr("src","/images/bullet.gif");
 $nextNav.attr("src","/images/bullet_active.gif");
}

function slideTo(id) {
    var $top = $('#quotes div:visible');
    var num = $top.attr('id').split('-').pop();
    var $topNav = $('#'+num);

    var $next =$('#'+id);
    num = $next.attr('id').split('-').pop();
    var $nextNav = $('#'+num);
  $top.fadeOut("slow", function() {
	 $next.fadeIn("slow");
  });
  $topNav.attr("src","/images/bullet.gif");
  $nextNav.attr("src","/images/bullet_active.gif"); 

}

$(document).ready(function() {
 init();
    //setInterval( "slide()", 15000 );
});
</script>
</div>