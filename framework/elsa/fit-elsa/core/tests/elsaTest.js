/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import {ELSA} from '../elsaEntry.js';
import {PAGE_MODE} from '../../common/const.js';
import {uuid} from '../../common/util.js';
import {elsaTest} from './testFramework.js';
import {addCommand, deleteCommand} from '../commands.js';
import {graph} from '../graph.js';

const repo = (() => {
  const repo = {};
  //mock
  repo.graphs = [];

  repo.saveGraph = graphData => {
    repo.graphs.push(graphData);
    return graphData;
  };
  repo.getGraph = graphId => {
    return repo.graphs.find(g => g.id === graphId);
  };
  return repo;
})();
const graphs = {};
let sessionData;

ELSA._mockRepo(repo);
ELSA._mockEmptyGraph((graphType, div) => {
  const session = uuid();
  const g = graph(div, '');
  g.sesion = self.session;
  const sendMessage = (message) => {
    graphs.sender.graph.collaboration.communicator.onMessage(message);
    graphs.receiver.graph.collaboration.communicator.onMessage(message);
  };
  //mock collaboration messages
  g.collaboration.invoke = args => {
    if (args.method === 'register_graph') {
      sessionData = JSON.parse(JSON.stringify(args.value));
      sessionData.isSessionData = true;//临时变量，用来验证第二次打开的graph得到了上传的数据
      return session;
    }
    if (args.method === 'load_graph') {
      if (sessionData) {
        return sessionData;
      } else {
        return null;
      }
    }
    if (args.method === 'new_page') {
      //模拟发送新增page消息
      const message = {};
      message.topic = 'page_added';
      message.page = args.page;
      message.value = args.value;
      sendMessage(message);
    }
    if (args.method === 'remove_page') {
      //模拟发送删除page消息
      const message = {};
      message.topic = 'page_removed';
      message.page = args.page;
      message.value = args.value;
      sendMessage(message);
    }
    if (args.method === 'change_page_index') {
      //模拟发送删除page消息
      const message = {};
      message.topic = 'page_index_changed';
      message.page = args.page;
      message.value = args.value;
      sendMessage(message);
    }
    if (args.method === 'new_shape') {
      //模拟发送删除page消息
      const message = {};
      message.topic = 'shape_added';
      message.page = args.page;
      message.value = args.value;
      sendMessage(message);
    }
    if (args.method === 'change_page_shape_data') {
      //模拟发送删除page消息
      const message = {};
      message.topic = 'page_shape_data_changed';
      message.page = args.page;
      message.value = args.value;
      sendMessage(message);
    }
    if (args.method === 'change_shape_index') {
      //模拟发送删除page消息
      const message = {};
      message.topic = 'shape_index_changed';
      message.page = args.page;
      message.shape = args.shape;
      message.value = args.value;
      sendMessage(message);
    }
    return undefined;
  };

  return g;
});

/**
 * graph test suite
 */
const graphTest = elsaTest.addTestSuite('graph test');
const mockGrpahs = async () => {
  const div = document.createElement('div');

  repo.graphs = [];//清空graphs
  sessionData = undefined;

  graphs.sender = await ELSA.newGraph(undefined, undefined, div);
  graphs.sender.graph = graphs.sender._graph;
  graphs.receiver = await ELSA.editGraph(graphs.sender.id, graphs.sender.type, div);
  graphs.receiver.graph = graphs.receiver._graph;

  return graphs;
};

graphTest.test('test get graph in display mode', async () => {
  const div = document.createElement('div');
  //create graph
  let newGraph = await ELSA.newGraph().then(id => ELSA.displayGraph(id, div));
  graphTest.assert(PAGE_MODE.DISPLAY, newGraph.getMode(), 'the graph should be in configuration mode');
  graphTest.assert('', newGraph.title, 'the title of empty graph is \'\' ');
});

graphTest.test('test edit graph in collaboration context', async () => {
  const graphs = await mockGrpahs();
  graphTest.assert(1, repo.graphs.length);
  graphTest.assertTrue(graphs.sender.graph.collaborationSession !== undefined, 'the graph should get collaboration server session id');
  graphTest.assertTrue(graphs.sender.graph.isSessionData === undefined, 'the firt graph data should be from repo');
  graphTest.assert(repo.getGraph(graphs.sender.id).title, graphs.sender.title, 'the graph info should have beend saved in repo');
  graphTest.assert(PAGE_MODE.CONFIGURATION, graphs.sender.mode, 'editing graph should be in CONFIGURATION mode');

  graphTest.assertTrue(graphs.receiver.graph.isSessionData, 'the second graph data should be from session data');
  graphTest.assert(graphs.receiver.title, graphs.sender.title, 'the second graph title should be same as the first graph title');
  graphTest.assert(graphs.receiver.id, graphs.sender.id, 'the second graph id should be same as the first graph id');
});

graphTest.test('test add new page should send new page command to collaboration server', async () => {
  const div = document.createElement('div');
  const name = 'page1';
  const id = 'page-1';
  const graphs = await mockGrpahs();
  graphs.sender.addPage(name, id, div);
  graphTest.assert(1, graphs.receiver.graph.pages.length, 'the second graph should create page following first graph create page command');
  graphTest.assert(1, graphs.sender.graph.pages.length, 'check the first graph will not response the second graph new page');
  graphTest.assert(name, graphs.receiver.graph.pages[0].text, 'the new page should have page command text');
  graphTest.assert(id, graphs.receiver.graph.pages[0].id, 'the new page should have page command id');
});

graphTest.test('test remove page should send remove page command to collaboration server', async () => {
  const div = document.createElement('div');
  const name = 'page1';
  const id = 'page-1';
  const graphs = await mockGrpahs();
  graphs.sender.addPage(name, id, div);
  graphTest.assert(1, graphs.receiver.graph.pages.length, 'confirm there is 1 page before remove it');
  graphs.receiver.graph.removePage(0);
  graphTest.assert(0, graphs.sender.graph.pages.length, 'there is 0 page in first graph after second graph remove the page');
});

graphTest.test('test move page index should send move page index command to collaboration server', async () => {
  const div = document.createElement('div');
  const graphs = await mockGrpahs();
  const page1 = graphs.sender.addPage('page1', 'page-1', div);
  const page2 = graphs.sender.addPage('page2', 'page-2', div);
  graphTest.assert(2, graphs.receiver.graph.pages.length, 'confirm there is 2 page in second graph after the first graph added 2 pages');
  graphTest.assert(0, graphs.sender.graph.getPageIndex(page1), 'confirm the initial page index');
  graphs.receiver.graph.changePageIndex(0, 1);
  graphTest.assert(1, graphs.sender.graph.getPageIndex(page1), 'the first graph pages should have changed index after the second graph changed the page index');

  //----------add some complicated redo/undo operation--------------------------------
  graphs.receiver.graph.getHistory().undo();
  graphTest.assert(0, graphs.sender.graph.getPageIndex(page1), 'after undo the second graph, the first graph page index should be undo as well');
  graphs.receiver.graph.getHistory().redo();
  graphTest.assert(1, graphs.sender.graph.getPageIndex(page1), 'after redo the second graph, the first graph page index should be redo as well');
  graphs.sender.graph.getHistory().undo();
  graphTest.assert(-1, graphs.sender.graph.getPageIndex(page2), 'after undo the first graph, will execute first graph undo to remove page2');
  graphTest.assert(0, graphs.sender.graph.getPageIndex(page1), 'check first graph page 1 index');
  graphTest.assert(1, graphs.receiver.graph.pages.length, 'the second graph should follow the undo, have only one page after first graph undo add page 2');
  graphs.sender.graph.getHistory().redo();
  graphTest.assert(1, graphs.sender.graph.getPageIndex(page1), 'after redo the first graph, add page2 back');
  graphs.sender.graph.deletePage(0);//delete page2
  graphTest.assert(-1, graphs.sender.graph.getPageIndex(page2), 'after first graph delete page2, the second graph should delete page2');
  graphs.sender.graph.getHistory().undo();
  graphTest.assert(0, graphs.sender.graph.getPageIndex(page2), 'after first graph undo, the second graph should put page2 back');
});

graphTest.test('test add and insert pages in a graph', async () => {
  //create graph
  let newGraph = (await mockGrpahs()).sender;
  newGraph.graph.pages = [];
  graphTest.assert(PAGE_MODE.CONFIGURATION, newGraph.getMode(), 'the graph should be in configuration mode');
  graphTest.assertTrue(newGraph.id !== undefined, 'new graph should have unique id');
  // graphTest.assert(0, newGraph.pages.length, "new graph is empty without any pages");

  // init graph
  const p1Name = 'page1';
  const p1Id = 'page-1';
  newGraph.addPage(p1Name, p1Id);
  graphTest.assert(1, newGraph.graph.pages.length, 'graph should have one page after invoke add page');

  //add first page
  const page1 = newGraph.graph.pages[0];
  graphTest.assert(p1Id, page1.id, 'check new page id');
  graphTest.assert(p1Name, page1.text, 'check new page text');
  graphTest.assert(0, page1.sm.getShapeCount(), 'new page should have 1 shapes');

  // insert second page before first page
  const p2Name = 'page2';
  const p2Id = 'page-2';
  newGraph.addPage(p2Name, p2Id, undefined, 0);
  const page2 = newGraph.graph.pages[0];
  graphTest.assert(p2Id, page2.id, 'check inserted index 0 page');
  graphTest.assert(2, newGraph.graph.pages.length, 'after insert 0 index page, graph should have 2 pages');
  graphTest.assert(page2.id, newGraph.graph.activePage.id, 'after insert 0 index page, active page of graph should the new page');
});

/**
 * graph test suite
 */
const pageTest = elsaTest.addTestSuite('page test');
pageTest.test('test add/modify/remove rectangle collaboration command', async () => {
  const div = document.createElement('div');
  const graphs = await mockGrpahs();
  graphs.sender.addPage('page1', 'page-1', div);
  const page1 = graphs.sender.graph.activePage;
  page1.stopAnimation();
  const page2 = graphs.receiver.graph.activePage;
  page2.stopAnimation();
  const rect1 = page1.createNew('rectangle', 100, 100);
  addCommand(page1, [{shape: rect1}]);
  const rect2 = page2.sm.getShapeById(rect1.id);
  pageTest.assertTrue(rect2 !== undefined, 'the second graph should create the rectangle follow the first graph');
  rect2.borderColor = 'red';
  rect2.backColor = 'blue';
  page2.sendChangedPageData();
  pageTest.assert(rect2.borderColor, rect1.borderColor, 'the first graph should follow second graph page border color change');
  pageTest.assert(rect2.backColor, rect1.backColor, 'the first graph should follow second graph page back color change');

  const rect1Data = graphs.sender.graph.pages[0].shapes[0];
  const rect2Data = graphs.receiver.graph.pages[0].shapes[0];
  pageTest.assert(rect1Data.backColor, rect1.backColor, 'check serialized data aligns to domain data');
  pageTest.assert(rect2Data.backColor, rect1.backColor);
  pageTest.assertTrue(rect1Data !== rect1);

  // rect1.remove();
  const cmd = deleteCommand(page1, [{shape: rect1}]);
  cmd.execute();
  page1.sendChangedPageData();
  pageTest.assert(0, page2.sm.getShapeCount(), 'the second page should follow first page to delete the rectangle');
  pageTest.assert(0, graphs.sender.graph.pages[0].shapes.length, 'raw data of the first graph should have deleted the shape');
  pageTest.assert(0, graphs.receiver.graph.pages[0].shapes.length, 'raw data of the second graph should have deleted the shape');

  page1.graph.getHistory().undo();
  page1.sendChangedPageData();
  pageTest.assert(1, page2.sm.getShapeCount(), 'the second page should follow first page to undo the delete of the rectangle');
  pageTest.assert(1, graphs.receiver.graph.pages[0].shapes.length);
  pageTest.assert(1, graphs.sender.graph.pages[0].shapes.length);

  page1.graph.getHistory().redo();
  page1.sendChangedPageData();
  pageTest.assert(0, page2.sm.getShapeCount(), 'the second page should follow first page to redo the delete of the rectangle');
  pageTest.assert(0, graphs.receiver.graph.pages[0].shapes.length);
  pageTest.assert(0, graphs.sender.graph.pages[0].shapes.length);

  page1.graph.getHistory().undo();
  //page1.sendChangedPageData();
  page1.graph.getHistory().undo();
  page1.sendChangedPageData();
  pageTest.assert(0, page2.sm.getShapeCount(), 'the second page should follow first page to undo create rectangle');
  pageTest.assert(0, graphs.receiver.graph.pages[0].shapes.length);
  pageTest.assert(0, graphs.sender.graph.pages[0].shapes.length);
});

pageTest.test('test change shape index to send collaboration command', async () => {
  const div = document.createElement('div');
  const graphs = await mockGrpahs();
  graphs.sender.addPage('page1', 'page-1', div);
  const page1 = graphs.sender.graph.activePage;
  const page2 = graphs.receiver.graph.activePage;
  page1.stopAnimation();
  const rect1 = page1.createNew('rectangle', 100, 100);
  const rect2 = page1.createNew('rectangle', 200, 100);

  pageTest.assert(100, rect1.getIndex());
  pageTest.assert(101, rect2.getIndex());
  pageTest.assert(rect1.id, graphs.receiver.graph.pages[0].shapes[0].id);
  pageTest.assert(rect2.id, graphs.receiver.graph.pages[0].shapes[1].id);

  page2.sm.updateShapes(writer => writer.moveShapeTo(page2.sm.shapes[1], 100));
  pageTest.assert(101, rect1.getIndex());
  pageTest.assert(100, rect2.getIndex());
  pageTest.assert(rect1.id, graphs.receiver.graph.pages[0].shapes[1].id);
  pageTest.assert(rect2.id, graphs.receiver.graph.pages[0].shapes[0].id);
});

pageTest.test('test paste shape to send collaboration command', async () => {
});

export {graphTest};