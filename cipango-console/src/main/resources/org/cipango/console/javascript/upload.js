
function check() 
{
  var ext = self.document.upload.installSar.value;
  ext = ext.substring(ext.length-4,ext.length);
  ext = ext.toLowerCase();
  if(ext != '.war' && ext != '.sar' && ext != '.zip' && ext != 'ssar') 
  {
    alert('You selected a ' + ext +
          ' file; please select a .war or .sar file instead!');
    return false; 
  } 
  else 
  {
    return true;
  }
}

function confirmAction(appName, url, displayText)
{
 var where_to= confirm("Please confirm to " + displayText + " the servlet application with URI prefix " + appName);
 if (where_to== true)
 {
   window.location=url;
 }
}