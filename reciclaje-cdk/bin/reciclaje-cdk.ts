#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { ReciclajeStack } from '../lib/reciclaje-stack';

const app = new cdk.App();

new ReciclajeStack(app, 'ReciclajeLitoralStack', {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION || 'us-east-1',
  },
  description: 'Infraestructura completa en AWS para la aplicacion Reciclaje Litoral',
});
