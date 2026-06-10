pipeline {
    agent any
    
    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '706877673423.dkr.ecr.ap-northeast-2.amazonaws.com'
        BACKEND_IMAGE = "${ECR_REGISTRY}/parking-backend"
        AI_IMAGE = "${ECR_REGISTRY}/parking-ai"
        SERVER_1_ID = 'i-0c41bdbc02e07418b'
        SERVER_2_ID = 'i-034723d6f74597dd5'
        TG_ARN = 'arn:aws:elasticloadbalancing:ap-northeast-2:706877673423:targetgroup/tg-parking-backend/0a4f548503cf98f6'
        ADMIN_BUCKET = 'parking-frontend-admin-v2'
        USER_BUCKET = 'parking-frontend-user-v2'
        KIOSK_BUCKET = 'parking-frontend-kiosk-v2'
        ADMIN_CF_ID = 'E3MLI2EH9A5Z1V'
        USER_CF_ID = 'E3R3NBJK0MRRKQ'
        KIOSK_CF_ID = 'E143U7V758QGPO'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git credentialsId: 'github-credentials',
                    url: 'https://github.com/dbsekdgh-dotcom/Final_Project_parking_system.git',
                    branch: 'develop'
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
                withCredentials([file(credentialsId: 'env-file-server1', variable: 'ENV_FILE')]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        sh """
                            # 프론트 빌드용 .env 복사
                            cp ${ENV_FILE} /var/lib/jenkins/.env

                            # Admin 빌드 및 배포
                            cd ./frontend/parking_frontend_admin
                            rm -rf node_modules
                            npm ci
                            npm run build
                            aws s3 sync dist/ s3://${ADMIN_BUCKET} --delete
                            aws cloudfront create-invalidation --distribution-id ${ADMIN_CF_ID} --paths "/*"
                            cd ../..

                            # User 빌드 및 배포
                            cd ./frontend/parking_frontend_user
                            rm -rf node_modules
                            npm ci
                            npm run build
                            aws s3 sync dist/ s3://${USER_BUCKET} --delete
                            aws cloudfront create-invalidation --distribution-id ${USER_CF_ID} --paths "/*"
                            cd ../..

                            # Kiosk 빌드 및 배포
                            cd ./frontend/parking_frontend_kiosk
                            rm -rf node_modules
                            npm ci
                            npm run build
                            aws s3 sync dist/ s3://${KIOSK_BUCKET} --delete
                            aws cloudfront create-invalidation --distribution-id ${KIOSK_CF_ID} --paths "/*"
                            cd ../..
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Server 2') {
            steps {
                withCredentials([
                    usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN'),
                    file(credentialsId: 'env-file-server2', variable: 'ENV_FILE')
                ]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        sh """
                            aws s3 cp ${ENV_FILE} s3://${ADMIN_BUCKET}/server.env
                        """
                        script {
                            def cmdId = sh(script: """
                                aws ssm send-command \
                                    --instance-ids ${SERVER_2_ID} \
                                    --document-name "AWS-RunShellScript" \
                                    --parameters '{"commands":["export HOME=/root && git config --global --add safe.directory /home/ssm-user/Final_Project_parking_system && cd /home/ssm-user/Final_Project_parking_system && git checkout docker-compose.yml && git pull https://${GIT_TOKEN}@github.com/dbsekdgh-dotcom/Final_Project_parking_system.git develop && aws s3 cp s3://parking-frontend-admin-v2/server.env .env && aws ecr get-login-password --region ap-northeast-2 | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY} && sudo docker stop parking-backend parking-ai ; sudo docker pull ${ECR_REGISTRY}/parking-backend:latest && sudo docker pull ${ECR_REGISTRY}/parking-ai:latest && sudo docker rm -f parking-backend parking-ai && sudo docker compose -f docker-compose.server2.yml up -d && sudo docker image prune -af"]}' \
                                    --region ${AWS_REGION} \
                                    --query 'Command.CommandId' \
                                    --output text
                            """, returnStdout: true).trim()
                            
                            // 최대 20분 대기 (30초 간격으로 40번)
                            def maxAttempts = 40
                            def attempt = 0
                            def status = 'InProgress'
                            
                            while (attempt < maxAttempts && status == 'InProgress') {
                                sleep 30
                                status = sh(script: """
                                    aws ssm get-command-invocation \
                                        --command-id ${cmdId} \
                                        --instance-id ${SERVER_2_ID} \
                                        --region ${AWS_REGION} \
                                        --query 'Status' \
                                        --output text
                                """, returnStdout: true).trim()
                                echo "SSM 상태: ${status} (시도 ${attempt + 1}/${maxAttempts})"
                                attempt++
                            }
                            
                            if (status != 'Success') {
                                def stdout = sh(script: """
                                    aws ssm get-command-invocation \
                                        --command-id ${cmdId} \
                                        --instance-id ${SERVER_2_ID} \
                                        --region ${AWS_REGION} \
                                        --query 'StandardOutputContent' \
                                        --output text
                                """, returnStdout: true).trim()
                                def stderr = sh(script: """
                                    aws ssm get-command-invocation \
                                        --command-id ${cmdId} \
                                        --instance-id ${SERVER_2_ID} \
                                        --region ${AWS_REGION} \
                                        --query 'StandardErrorContent' \
                                        --output text
                                """, returnStdout: true).trim()
                                echo "=== Server 2 STDOUT ===\n${stdout}"
                                echo "=== Server 2 STDERR ===\n${stderr}"
                                error "SSM 명령 실패: ${status}"
                            }
                        }
                    }
                }
            }
        }

        stage('Health Check Server 2') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    script {
                        def maxAttempts = 10
                        def attempt = 0
                        def health = ''
                        while (attempt < maxAttempts) {
                            sleep 30
                            health = sh(script: """
                                aws elbv2 describe-target-health \
                                    --target-group-arn ${TG_ARN} \
                                    --region ${AWS_REGION} \
                                    --query "TargetHealthDescriptions[?Target.Id=='${SERVER_2_ID}'].TargetHealth.State" \
                                    --output text
                            """, returnStdout: true).trim()
                            echo "Server 2 health: ${health} (시도 ${attempt + 1}/${maxAttempts})"
                            if (health == 'healthy') break
                            attempt++
                        }
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
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    sh """
                        aws elbv2 register-targets \
                            --target-group-arn ${TG_ARN} \
                            --targets Id=${SERVER_1_ID} \
                            --region ${AWS_REGION}
                    """
                }
                withCredentials([
                    usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN'),
                    file(credentialsId: 'env-file-server1', variable: 'ENV_FILE')
                ]) {
                    withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                        sh """
                            aws s3 cp ${ENV_FILE} s3://${ADMIN_BUCKET}/server.env
                        """
                        script {
                            def cmdId = sh(script: """
                                aws ssm send-command \
                                    --instance-ids ${SERVER_1_ID} \
                                    --document-name "AWS-RunShellScript" \
                                    --parameters '{"commands":["export HOME=/root && git config --global --add safe.directory /home/ssm-user/Final_Project_parking_system && cd /home/ssm-user/Final_Project_parking_system && git checkout docker-compose.yml && git pull https://${GIT_TOKEN}@github.com/dbsekdgh-dotcom/Final_Project_parking_system.git develop && aws s3 cp s3://parking-frontend-admin-v2/server.env .env && aws ecr get-login-password --region ap-northeast-2 | sudo docker login --username AWS --password-stdin ${ECR_REGISTRY} && sudo docker stop parking-backend parking-ai ; sudo docker pull ${ECR_REGISTRY}/parking-backend:latest && sudo docker pull ${ECR_REGISTRY}/parking-ai:latest && sudo docker rm -f parking-backend parking-ai && sudo docker compose up -d && sudo docker image prune -af"]}' \
                                    --region ${AWS_REGION} \
                                    --query 'Command.CommandId' \
                                    --output text
                            """, returnStdout: true).trim()
                            
                            // 최대 20분 대기 (30초 간격으로 40번)
                            def maxAttempts = 40
                            def attempt = 0
                            def status = 'InProgress'
                            
                            while (attempt < maxAttempts && status == 'InProgress') {
                                sleep 30
                                status = sh(script: """
                                    aws ssm get-command-invocation \
                                        --command-id ${cmdId} \
                                        --instance-id ${SERVER_1_ID} \
                                        --region ${AWS_REGION} \
                                        --query 'Status' \
                                        --output text
                                """, returnStdout: true).trim()
                                echo "SSM 상태: ${status} (시도 ${attempt + 1}/${maxAttempts})"
                                attempt++
                            }
                            
                            if (status != 'Success') {
                                def stdout = sh(script: """
                                    aws ssm get-command-invocation \
                                        --command-id ${cmdId} \
                                        --instance-id ${SERVER_1_ID} \
                                        --region ${AWS_REGION} \
                                        --query 'StandardOutputContent' \
                                        --output text
                                """, returnStdout: true).trim()
                                def stderr = sh(script: """
                                    aws ssm get-command-invocation \
                                        --command-id ${cmdId} \
                                        --instance-id ${SERVER_1_ID} \
                                        --region ${AWS_REGION} \
                                        --query 'StandardErrorContent' \
                                        --output text
                                """, returnStdout: true).trim()
                                echo "=== Server 1 STDOUT ===\n${stdout}"
                                echo "=== Server 1 STDERR ===\n${stderr}"
                                error "SSM 명령 실패: ${status}"
                            }
                        }
                    }
                }
            }
        }

        stage('Health Check Server 1') {
            steps {
                withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
                    script {
                        def maxAttempts = 10
                        def attempt = 0
                        def health = ''
                        while (attempt < maxAttempts) {
                            sleep 30
                            health = sh(script: """
                                aws elbv2 describe-target-health \
                                    --target-group-arn ${TG_ARN} \
                                    --region ${AWS_REGION} \
                                    --query "TargetHealthDescriptions[?Target.Id=='${SERVER_1_ID}'].TargetHealth.State" \
                                    --output text
                            """, returnStdout: true).trim()
                            echo "Server 1 health: ${health} (시도 ${attempt + 1}/${maxAttempts})"
                            if (health == 'healthy') break
                            attempt++
                        }
                        if (health != 'healthy' && health != 'initial') {
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
        always {
            sh 'docker system prune -f'
        }
        success {
            echo '배포 성공!'
        }
        failure {
            withAWS(credentials: 'aws-credentials', region: "${AWS_REGION}") {
            sh """
                aws elbv2 register-targets \
                    --target-group-arn ${TG_ARN} \
                    --targets Id=${SERVER_1_ID} \
                    --region ${AWS_REGION}
            """
            }
            echo '배포 실패! Server 1을 대상 그룹에 복구했습니다.'
        }
    }
}