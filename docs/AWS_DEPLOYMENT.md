# AWS Deployment Guide

This guide covers deploying the Event-Driven E-commerce Marketplace Simulator to AWS using free tier eligible services.

## Architecture Options

### Option 1: AWS App Runner (Simplest - Recommended for Demo)

**Pros:**
- Serverless, auto-scaling
- Free tier: 5 GB-hours/month
- Simple deployment from container images
- No infrastructure management

**Cons:**
- Limited customization
- May exceed free tier with multiple services

### Option 2: AWS ECS Fargate (Balanced)

**Pros:**
- Full container orchestration
- Free tier eligible (t2.micro equivalent)
- More control than App Runner
- Good for production-like demos

**Cons:**
- More complex setup
- Requires VPC, load balancers

### Option 3: EC2 Free Tier (Most Control)

**Pros:**
- Full control over infrastructure
- Free tier: 750 hours/month of t2.micro
- Can run everything on one instance for demo

**Cons:**
- Manual setup and maintenance
- Single point of failure

## Recommended Setup: ECS Fargate + RDS + MSK

### Prerequisites

- AWS CLI configured
- Docker installed locally
- AWS account with free tier eligibility

### Step 1: Build and Push Docker Images

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Create ECR repositories
aws ecr create-repository --repository-name order-service --region us-east-1
aws ecr create-repository --repository-name inventory-service --region us-east-1
aws ecr create-repository --repository-name payment-service --region us-east-1
aws ecr create-repository --repository-name frontend --region us-east-1

# Build and push images
cd order-service && docker build -t order-service .
docker tag order-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/order-service:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/order-service:latest

# Repeat for other services...
```

### Step 2: Create RDS PostgreSQL Instance

```bash
aws rds create-db-instance \
  --db-instance-identifier ecommerce-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username postgres \
  --master-user-password <secure-password> \
  --allocated-storage 20 \
  --region us-east-1
```

**Note:** db.t3.micro is free tier eligible for 750 hours/month.

### Step 3: Create Amazon MSK Cluster (or use EC2 for Kafka)

For free tier, consider running Kafka on EC2 instead of MSK (MSK is not free tier eligible).

**Alternative: Kafka on EC2**

```bash
# Launch EC2 t2.micro instance
# Install Docker and run Kafka via docker-compose
# Or use Confluent Platform Community Edition
```

### Step 4: Create ECS Cluster and Task Definitions

```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name ecommerce-cluster --region us-east-1

# Create task definitions (JSON files) for each service
# See examples in infrastructure/ecs/ directory
```

### Step 5: Create Application Load Balancer

```bash
# Create ALB for frontend and API gateway
aws elbv2 create-load-balancer \
  --name ecommerce-alb \
  --subnets subnet-xxx subnet-yyy \
  --security-groups sg-xxx \
  --region us-east-1
```

### Step 6: Configure Security Groups

- **ALB Security Group:** Allow HTTP/HTTPS from internet
- **ECS Security Group:** Allow traffic from ALB only
- **RDS Security Group:** Allow PostgreSQL (5432) from ECS security group
- **Kafka Security Group:** Allow Kafka (9092) from ECS security group

### Step 7: Environment Variables

Update ECS task definitions with:
- Database connection strings (RDS endpoint)
- Kafka bootstrap servers
- Service URLs for frontend

### Step 8: Deploy Frontend

Option A: Deploy via S3 + CloudFront (Static Hosting)
```bash
cd frontend
npm run build
aws s3 sync dist/ s3://your-bucket-name
aws cloudfront create-distribution --origin-domain-name your-bucket-name.s3.amazonaws.com
```

Option B: Deploy via ECS (Container)

## Cost Estimation (Free Tier)

- **RDS db.t3.micro:** Free for 750 hours/month
- **EC2 t2.micro:** Free for 750 hours/month (for Kafka)
- **ECS Fargate:** ~$0.04/hour per service (not free tier, but minimal)
- **ALB:** ~$0.0225/hour (not free tier)
- **Data Transfer:** 1 GB/month free

**Total Estimated Cost:** ~$10-20/month if exceeding free tier limits

## Simplified Single-EC2 Deployment (Free Tier Friendly)

For a true free-tier demo, deploy everything on a single EC2 t2.micro:

1. Launch EC2 t2.micro instance (Ubuntu 22.04)
2. Install Docker and Docker Compose
3. Clone repository
4. Update docker-compose.yml with production settings
5. Run `docker-compose up -d`

**Limitations:**
- Single instance (no high availability)
- Limited resources (1 vCPU, 1GB RAM)
- May need to reduce resource limits in docker-compose.yml

## Monitoring

- **CloudWatch:** Monitor service logs and metrics
- **ECS Service:** View task status and logs
- **RDS:** Monitor database performance

## Troubleshooting

1. **Services can't connect to Kafka:** Check security groups and network ACLs
2. **Database connection failures:** Verify RDS security group allows ECS security group
3. **High costs:** Review CloudWatch metrics, consider stopping services when not in use

## Cleanup

To avoid charges, delete:
- ECS services and tasks
- RDS instance (or take snapshot first)
- ECR images
- ALB
- EC2 instances
- CloudWatch log groups

```bash
# Stop ECS services
aws ecs update-service --cluster ecommerce-cluster --service order-service --desired-count 0

# Delete RDS (creates final snapshot)
aws rds delete-db-instance --db-instance-identifier ecommerce-db --skip-final-snapshot
```
