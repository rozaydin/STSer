# STSer
Automatically adds STS Tokens 

* AWS_ACCESS_KEY_ID
* AWS_SECRET_ACCESS_KEY
* AWS_SECURITY_TOKEN
* AWS_SESSION_TOKEN

to project run/debug settings (by default only selected/active run/debug setting is modified) as environment variables. 
Plugin searches STS config file under ${user.home}/Downloads/sts.txt path user.home variable resolved using java call System.getProperty("user.home");


Application and SpringBootApplication run configurations are supported

## v1.1
Fixed a bug that caused freezes on the IDE.

