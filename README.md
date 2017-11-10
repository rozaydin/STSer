# STSer
Automatically adds STS Tokens 

* AWS_ACCESS_KEY_ID
* AWS_SECRET_ACCESS_KEY
* AWS_SECURITY_TOKEN
* AWS_SESSION_TOKEN

to project run/debug settings (by default all available run/debug settings are modified) as environment variables. 
Plugin searches STS.txt file under ${user.home}/STS/sts.txt path user.home variable resolved using java call System.getProperty("user.home");

