pipeline {
	agent none
    options {
        disableConcurrentBuilds()
    }
    stages {
    	stage('Prepare Maven stages') {
	    	agent {
		        dockerfile {
					filename 'Dockerfile'
					dir 'docker'
					additionalBuildArgs '--build-arg USER_ID=$(id -u) --build-arg GROUP_ID=$(id -g)'
					args '-v ${HOME}/.ssh:${HOME}/.ssh -v ${HOME}/.m2:${HOME}/.m2'
		        }
		    }
			environment {
		    	MVN_SSL_OPTS = '-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true'
		    	MVN_DOCLINT_OPTS = 'org.apache.maven.plugins:maven-javadoc-plugin:3.1.0:jar -DadditionalJOption=-Xdoclint:none'
    		}
	    	stages {
		    	stage('Build') {
		    		steps {
		    			sh 'mvn $MVN_SSL_OPTS -DskipTests -B clean package'
		    		}
		    	}
		    	stage('Test') {
		    		steps {
		    			sh 'mvn $MVN_SSL_OPTS test'
		    		}
		    	}
		    	stage('Deploy snapshot') {
					when {
						branch 'develop'
					}
					steps {
						sh 'mvn $MVN_SSL_OPTS $MVN_DOCLINT_OPTS -DskipTests source:jar deploy -DaltDeploymentRepository=rma.snapshot::default::https://mvn.marvin.vito.local/repository/rma-snapshot'
					}
		    	}
		    	stage('Deploy release') {
					when {
						branch 'master'
					}
					steps {
						sh 'mvn $MVN_SSL_OPTS $MVN_DOCLINT_OPTS -DskipTests source:jar deploy -DaltDeploymentRepository=rma.release::default::https://mvn.marvin.vito.local/repository/rma'
					}
		    	}
		    	stage('Build docker images') {
					steps {
						sh '''#!/bin/bash
							cd resttools-demo-server
							mvn $MVN_SSL_OPTS compile jib:buildTar
							# fetch metadata from maven
							mvn $MVN_SSL_OPTS org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | tail | grep -v INFO > target/version.txt
							mvn $MVN_SSL_OPTS org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.artifactId 2>/dev/null | tail | grep -v INFO > target/artifactId.txt
						'''
					}    		
		    	}
		    }
		}
		stage ('Prepare Docker stages') {
			agent any
			environment {
				registry = "rma-tools-docker-local.repo.vito.be"
				registryCredential = "svc_git_rma"
				version = sh(script: "cat resttools-demo-server/target/version.txt", returnStdout: true).trim()
				artifactId = sh(script: "cat resttools-demo-server/target/artifactId.txt", returnStdout: true).trim()
				timestamp = sh(script: "date +%Y%m%d.%H%M%S", returnStdout: true).trim()
			}
			stages {
		    	stage('Deploy snapshot docker images') {
		    		when {
		    			branch 'develop'
		    		}
		    		steps {
		    			script {
							sh '''#!/bin/bash
								cd resttools-demo-server
								docker load --input target/jib-image.tar
								docker tag $artifactId:$version $artifactId:$version-$timestamp
							'''
							dockerImage = docker.image("$artifactId:$version-$timestamp")
							docker.withRegistry("https://" + registry, registryCredential ) {
								dockerImage.push("$version-$timestamp")
								dockerImage.push("$version")
							}
							sh '''#!/bin/bash
								docker image rm $registry/$artifactId:$version-$timestamp
								docker image rm $registry/$artifactId:$version
								docker image rm $artifactId:$version-$timestamp
								docker image rm $artifactId:$version
								docker image rm $artifactId:latest
							'''
						}
		    		}
		    	}
		    	stage('Deploy release docker images') {
		    		when {
		    			branch 'master'
		    		}
		    		steps {
		    			script {
							sh '''#!/bin/bash
								cd resttools-demo-server
								docker load --input target/jib-image.tar
							'''
							dockerImage = docker.image("$artifactId:$version")
							docker.withRegistry("https://" + registry, registryCredential ) {
								dockerImage.push("$version")
							}
							sh '''#!/bin/bash
								docker image rm $registry/$artifactId:$version
								docker image rm $artifactId:$version
								docker image rm $artifactId:latest
							'''
						}
		    		}
		    	}
		    }
    	}
    }
}
