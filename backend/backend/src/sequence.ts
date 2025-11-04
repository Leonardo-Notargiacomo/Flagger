import {MiddlewareSequence, RequestContext} from '@loopback/rest';

export class MySequence extends MiddlewareSequence {
  async handle(context: RequestContext) {
    try {
      const {request} = context;

      // Log incoming request
      console.log('\n==================== INCOMING REQUEST ====================');
      console.log(`[${new Date().toISOString()}] ${request.method} ${request.url}`);
      console.log('=========================================================\n');

      await super.handle(context);
    } catch (err) {
      console.error('\n==================== ERROR ====================');
      console.error(`[${new Date().toISOString()}] Error:`, err);
      console.error('===============================================\n');
      throw err;
    }
  }
}
