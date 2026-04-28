pipeline {
    agent any
    
    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '079092240156.dkr.ecr.ap-northeast-2.amazonaws.com'
        BACKEND_IMAGE = "${ECR_REGISTRY}/parking-backend"
        AI_IMAGE = "${ECR_REGISTRY}/parking-ai"
        SERVER_1_ID = 'i-019f4c49b578a0a28'
        SERVER_2_ID = 'i-039b40dc35867bbaf'
        TG_ARN = 'arn:aws:elasticloadbalancing:ap-northeast-2:079092240156:targetgroup/tg-parking-backend/9e1caa6a2b242dc7'
        ADMIN_BUCKET = 'parking-frontend-admin'
        USER_BUCKET = 'parking-frontend-user'
        KIOSK_BUCKET = 'parking-frontend-kiosk'
        ADMIN_CF_ID = 'E11EOPU9PR3ODN'
        USER_CF_ID = 'E1IRC5OJEGAFW8'
        KIOSK_CF_ID = 'EEQOV34NAOFWV'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'github-credentials',
                    url: 'https://github.com/dbsekdgh-dotcom/Final_Project_parking_system.git',
                    branch: 'new-branch-bokyung'
            }
        }
        
        stage('ECR Login') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """
                }
            }
        }
        
        stage('Build & Push Backend') {
            steps {
                sh """
                    docker build --no-cache -t ${BACKEND_IMAGE}:latest ./backend/parking_backend
                    docker push ${BACKEND_IMAGE}:latest
                """
            }
        }
        
        stage('Build & Push AI') {
            steps {
                sh """
                    docker build -t ${AI_IMAGE}:latest ./ai-service
                    docker push ${AI_IMAGE}:latest
                """
            }
        }

        stage('Build & Deploy Frontend') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        # Admin 빌드 및 배포
                        cd ./frontend/parking_frontend_admin
                        rm -rf node_modules
                        /home/ssm-user/.nvm/versions/node/v20.20.2/bin/npm ci
                        /home/ssm-user/.nvm/versions/node/v20.20.2/bin/npm run build
                        aws s3 sync dist/ s3://${ADMIN_BUCKET} --delete
                        aws cloudfront create-invalidation --distribution-id ${ADMIN_CF_ID} --paths "/*"
                        cd ../..

                        # User 빌드 및 배포
                        cd ./frontend/parking_frontend_user
                        rm -rf node_modules
                        /home/ssm-user/.nvm/versions/node/v20.20.2/bin/npm ci
                        /home/ssm-user/.nvm/versions/node/v20.20.2/bin/npm run build
                        aws s3 sync dist/ s3://${USER_BUCKET} --delete
                        aws cloudfront create-invalidation --distribution-id ${USER_CF_ID} --paths "/*"
                        cd ../..

                        # Kiosk 빌드 및 배포
                        cd ./frontend/parking_frontend_kiosk
                        rm -rf node_modules
                        /home/ssm-user/.nvm/versions/node/v20.20.2/bin/npm ci
                        /home/ssm-user/.nvm/versions/node/v20.20.2/bin/npm run build
                        aws s3 sync dist/ s3://${KIOSK_BUCKET} --delete
                        aws cloudfront create-invalidation --distribution-id ${KIOSK_CF_ID} --paths "/*"
                        cd ../..
                    """
                }
            }
        }
        
        stage('Deploy to Server 2') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        script {
                            def cmdId = sh(script: """
                                aws ssm send-command \
                                    --instance-ids ${SERVER_2_ID} \
                                    --document-name "AWS-RunShellScript" \
                                    --parameters '{"commands":["export HOME=/root && git config --global --add safe.directory /home/ssm-user/Final_Project_parking_system && cd /home/ssm-user/Final_Project_parking_system && git checkout docker-compose.yml && git pull https://${GIT_TOKEN}@github.com/dbsekdgh-dotcom/Final_Project_parking_system.git new-branch-bokyung && aws ecr get-login-password --region ap-northeast-2 | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY} && sudo docker compose -f docker-compose.server2.yml pull && sudo docker rm -f parking-backend && sudo docker compose -f docker-compose.server2.yml up -d && sudo docker image prune -f"]}' \
                                    --region ${AWS_REGION} \
                                    --query 'Command.CommandId' \
                                    --output text
                            """, returnStdout: true).trim()
                            
                            sh """
                                aws ssm wait command-executed \
                                    --command-id ${cmdId} \
                                    --instance-id ${SERVER_2_ID} \
                                    --region ${AWS_REGION}
                            """
                        }
                    }
                }
            }
        }
        
        stage('Health Check Server 2') {
            steps {
                sh 'sleep 60'
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    script {
                        def health = sh(script: """
                            aws elbv2 describe-target-health \
                                --target-group-arn ${TG_ARN} \
                                --region ${AWS_REGION} \
                                --query 'TargetHealthDescriptions[?Target.Id==\\"${SERVER_2_ID}\\"].TargetHealth.State' \
                                --output text
                        """, returnStdout: true).trim()
                        
                        echo "Server 2 health: ${health}"
                        
                        if (health != 'healthy') {
                            error "Server 2 is ${health}. 배포를 중단합니다."
                        }
                    }
                }
            }
        }
        
        stage('Switch Traffic to Server 2') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws elbv2 register-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_2_ID} \
                            --region ${AWS_REGION}
                        aws elbv2 deregister-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_1_ID} \
                            --region ${AWS_REGION}
                    """
                }
            }
        }
        
        stage('Deploy to Server 1') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        script {
                            def cmdId = sh(script: """
                                aws ssm send-command \
                                    --instance-ids ${SERVER_1_ID} \
                                    --document-name "AWS-RunShellScript" \
                                    --parameters '{"commands":["export HOME=/root && git config --global --add safe.directory /home/ssm-user/Final_Project_parking_system && cd /home/ssm-user/Final_Project_parking_system && git checkout docker-compose.yml && git pull https://${GIT_TOKEN}@github.com/dbsekdgh-dotcom/Final_Project_parking_system.git new-branch-bokyung && aws ecr get-login-password --region ap-northeast-2 | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY} && sudo docker compose pull && sudo docker rm -f parking-backend && sudo docker compose up -d && sudo docker image prune -f"]}' \
                                    --region ${AWS_REGION} \
                                    --query 'Command.CommandId' \
                                    --output text
                            """, returnStdout: true).trim()
                            
                            sh """
                                aws ssm wait command-executed \
                                    --command-id ${cmdId} \
                                    --instance-id ${SERVER_1_ID} \
                                    --region ${AWS_REGION}
                            """
                        }
                    }
                }
            }
        }

        stage('Health Check Server 1') {
            steps {
                sh 'sleep 60'
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    script {
                        def health = sh(script: """
                            aws elbv2 describe-target-health \
                                --target-group-arn ${TG_ARN} \
                                --region ${AWS_REGION} \
                                --query 'TargetHealthDescriptions[?Target.Id==\\"${SERVER_1_ID}\\"].TargetHealth.State' \
                                --output text
                        """, returnStdout: true).trim()
                        
                        echo "Server 1 health: ${health}"
                        
                        if (health != 'healthy') {
                            error "Server 1 is ${health}. 배포를 중단합니다."
                        }
                    }
                }
            }
        }
        
        stage('Switch Traffic to Both') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws elbv2 register-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_1_ID} \
                            --region ${AWS_REGION}
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo '배포 성공!'
        }
        failure {
            echo '배포 실패!'
        }
    }
}