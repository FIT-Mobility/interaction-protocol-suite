# OMP project to WSDL converter maven plugin

This module contains a maven plugin that can be integrated into the build pipeline to convert a project (as json) to a WSDL file describing the web service and a SOAP 1.2 binding.
Since the WSDL file contains the host information, this information can be given to the plugin in addition to the project file itself.
Otherwise the hostname is assumed to be `http://localhost:8080/ws`.
Additionally, the output directory can be configured.
