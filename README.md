# API Connect policy uploader
Programmatically upload custom policies to an API Connect catalog

The product IBM API Connect in version 5 does not support uploading of custom policies via restful services. If you want to create organizations or catalogs dynamically you might need to install policies dynamically, too.
This application will help you to do this task.

It was created by capturing the HTTP transprot between the browser and the API Connect management UI and then simulating the needed calls programmatically.

## Usage
### Parameters

To upload a policy package all neede files in a ZIP file just as if you want to upload them via your web browser. Then call this small application and specify the neccessary parameters:

     -s (--server) URL        : The server where to install the policy
     -o (--organization) VAL  : The API Connect organization where to install the policy
     -c (--catalog) VAL       : The API Connect catalog where to install the policy
     -pf (--policyFile) PATH  : Path to file containing policy definition
     --ignoreSSL              : Ignore SSL check while policy installation (default: false)

### Credentials
You will also need to specify username and password for accessing the management UI. You have three possibilities to provide the credentials. Either give them plain text in the command line:

    -p (--password) VAL          : The password to use for policy installation
    -u (--username) VAL          : The username to use for policy installation

Or use the base64 of ``username:password`` in the command line just like any HTTP basic authentication: 

    -cf (--credentialsFile) PATH : Path to file containing credentials information in HTTP authentication format

Or provide the credentials in a file and provide the file name as command line parameter. The content of the file is expected to be base64 of ``username:password`` just like basic HTTP authentication:

    -ch (--credentialsHttp) VAL  : The cretentials to be used for policy installation in HTTP authentication format. Format: base64(username:password)

### Example
An example for the parameters could look like this: ``-s https://api-management-deve.hlag.com -u testuser -p password  -o 5ad6e357e4b0418c227abcdd -c 5b69846aeff241b3069aecb4 --ignoreSSL -pf "./custom-policies/logPolicy/writeToDatapowerLog.zip"``


