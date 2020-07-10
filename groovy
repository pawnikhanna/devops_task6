job('task6_Job1') {
    description('Job1')
	scm {
        	github('pawnikhanna/task6_webcode','master')
    }
    steps {
       		shell("cp * -vrf /home/jenkins") 
     	}
  	triggers {
        	scm('* * * * *')
	    	}
	triggers {
        		upstream('Admin Job (Seed)', 'SUCCESS')
    			}
}

job('task6_Job2') {
    description('Job2')
	scm {
        	github('pawnikhanna/devops_task6','master')
    		}
	triggers {
        		upstream('G Job1', 'SUCCESS')
    			}
	steps {
       		shell('''
			if  ls /home/jenkins | grep php
			then
			 	if kubectl get deployment --selector "app in (httpd)" | grep httpd-web
    				then
			 		kubectl apply -f Deployment.yml
    			 	else
                 			kubectl create -f Deployment.yml
    			 	fi
    			 	POD=$(kubectl get pod -l app=httpd -o jsonpath="{.items[0].metadata.name}")
    			 	kubectl cp /home/jenkins/index.php ${POD}:/var/www/html
			fi
			''') 
     		}
  
}
job('task6_Job3') {
    description('Job3')
	triggers {
        		upstream('G Job2', 'SUCCESS')
    			}
    steps {
       	shell('''
		status=$(curl -o /dev/null -s -w "%{http_code}" http://192.168.99.100:30001)
		if [[ $status == 200 ]]
		then
			exit 0
		else
			exit 1
	    	fi
		''') 
     			}
  publishers {
        extendedEmail {
            recipientList('pawnikhanna12@gmail.com')
            defaultSubject('Job status')
          	attachBuildLog(attachBuildLog = true)
            defaultContent('Status Report')
            contentType('text/html')
            triggers {
                always {
                    subject('build Status')
                    content('Body')
                    sendTo {
                        developers()
                        recipientList()
                    }
                }
            }
        }
    }
}
