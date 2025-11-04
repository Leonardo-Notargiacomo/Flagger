import {inject} from '@loopback/core';
import {
  FindRoute,
  InvokeMethod,
  InvokeMiddleware,
  ParseParams,
  Reject,
  RequestContext,
  Send,
  SequenceHandler,
} from '@loopback/rest';

export class MySequence implements SequenceHandler {
  constructor(
    @inject('middleware.chain') protected middlewareChain: InvokeMiddleware,
    @inject('sequence.actions.findRoute') protected findRoute: FindRoute,
    @inject('sequence.actions.parseParams') protected parseParams: ParseParams,
    @inject('sequence.actions.invoke') protected invoke: InvokeMethod,
    @inject('sequence.actions.send') protected send: Send,
    @inject('sequence.actions.reject') protected reject: Reject,
  ) {}

  async handle(context: RequestContext) {
    try {
      const {request, response} = context;

      // Log incoming request
      console.log('\n==================== INCOMING REQUEST ====================');
      console.log(`[${new Date().toISOString()}] ${request.method} ${request.url}`);
      console.log('Headers:', JSON.stringify(request.headers, null, 2));
      if (request.body) {
        console.log('Body:', JSON.stringify(request.body, null, 2));
      }
      console.log('=========================================================\n');

      await this.middlewareChain(context);

      const route = this.findRoute(request);
      const args = await this.parseParams(request, route);
      const result = await this.invoke(route, args);

      // Log response
      console.log('\n==================== OUTGOING RESPONSE ====================');
      console.log(`[${new Date().toISOString()}] ${request.method} ${request.url}`);
      console.log('Status:', response.statusCode);
      console.log('Result:', JSON.stringify(result, null, 2));
      console.log('==========================================================\n');

      this.send(response, result);
    } catch (err) {
      console.error('\n==================== ERROR ====================');
      console.error(`[${new Date().toISOString()}] Error handling request:`, err);
      console.error('===============================================\n');
      this.reject(context, err);
    }
  }
}
