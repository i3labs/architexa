

<div style="margin-top: 15px;">
<table style="width:100%">
<tr valign="top" style="vertical-align:top;" >
<td>
<span id="forumTitle"><a id="forumTitle" href="<?php bb_uri(); ?>"><?php bb_option('name'); ?></a></span>
</td><td align="right" style="text-align:right;">
	<div style="float:right;">
	<?php if ( !in_array( bb_get_location(), array( 'login-page', 'register-page' ) ) ) login_form(); ?>
	</div>
</td>
</tr>
</table>
</div>


<div style="clear:both;"></div>

	<div style="float:right;" class="search">
		<?php search_form(); ?>
	</div>
	
