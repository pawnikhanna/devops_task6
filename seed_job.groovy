job("task6_Job1") 
     {
	description("Job1")
	keepDependencies(false)
	scm {
		git {
			remote {
				github("pawnikhanna/devops_task6", "https")
			}
			branch("*/master")
		}
	}
	disabled(false)
	triggers {
		scm("* * * * *") {
			ignorePostCommitHooks(false)
		}
	}
	concurrentBuild(false)
	steps {
		shell('sudo cp -rvf * /root/web')
	}
}

job("task6_Job2")
{
description ("Job2")
steps{
shell("""
if sudo kubectl get all | grep apache
then
echo "service exist"
sudo kubectl delete all --all
sudo kubectl delete httpd-pv-claim1
else
sudo kubectl create -f /root/web/deployment.yml
fi 
sudo kubectl get all
 """)
}
triggers {
   upstream('task6_Job1', 'SUCCESS')
     }
  }
job("task6_Job3") {
	description("Job3")
	
	triggers {
	        
	        upstream {
	            upstreamProjects('task6_Job2')
	            threshold('SUCCESS')
	        }
	    }
	steps {
		shell("""status=\$(curl -sL -w "%{http_code}" -I "http://192.168.99.100:30001" -o /dev/null)
if [[ \$status == 200 ]]
then
exit 0
else
exit 1
fi""")
}
	
job("task6_Job4")
{
description ("Job4")
 authenticationToken('mail')
   publishers {
		mailer("pawnikhanna@gmail.com", false, false)
	}
   triggers {
   upstream('task6_Job3', 'SUCCESS')
   }
   }
buildPipelineView('task6 pipeline') {
    filterBuildQueue()
    filterExecutors()
    title('Task6 Pipeline')
    displayedBuilds(3)
    selectedJob('task6_Job1')
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
    }
}
