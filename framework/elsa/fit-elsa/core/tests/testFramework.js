/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

const elsaTest = (() => {
    const testSuites = {};
    const self = {};
    self.display;
    self.onlyError = false;
    self.addTestSuite = name => {
        const tests = {cases: []};
        tests.test = (description, func) => {
            tests.cases.push({description, func});
        };

        tests.info = (message, first) => {
            if (self.onlyError) {
                return;
            }

            self.display && (self.display.innerHTML += (first ? "" : "</br>") + "<font color='dimgray'>▷-----" + message + "----- </font></br>");
        }
        tests.assert = (should, actual, description) => {
          let descriptionVal = description;
          if (descriptionVal === undefined) {
            descriptionVal = `test value should be ${should}`;
            }
            if (should === actual) {
                if (!self.onlyError) {
                    self.display && (self.display.innerHTML += `✔${descriptionVal} [passed......]</br>`);
                }
            } else {
                const err = `${descriptionVal} [failed......]`;
                const detail = `expected value is: ${should}, actual value is:${actual}`;
                self.display && (self.display.innerHTML += "<font color='red'>✘" + err + "</br>" + detail + "</font></br>");
                self.isSuccess = false;
            }
        };

        tests.assertTrue = (actual, description) => {
            tests.assert(true, actual, description);
        };

        tests.assertFalse = (actual, description) => {
            tests.assert(false, actual, description);
        };

        testSuites[name] = tests;
        return tests;
    };
    self.runAll = async () => {
        for (let t in testSuites) {
            await self.run(t);
        }
    };

    self.run = async name => {
        const tests = testSuites[name];
        tests.before && tests.before();
        let sucess = 0;
        let fail = 0;
        self.display && (self.display.innerHTML += "<h4><font color='steelblue'>☼---------------- begin test " + name + "----------------☼</h4>");
        for (let i = 0; i < tests.cases.length; i++) {
            const t = tests.cases[i];
            self.display && (self.display.innerHTML += "<h5>▶ test case: " + t.description + "....</h4>");
            try {
                self.isSuccess = true;
                await t.func();
                if (self.isSuccess) {
                    sucess++;
                } else {
                    fail++;
                }
            } catch (e) {
                self.display && (self.display.innerHTML += "<font color='red'>★★★★------------------------system error--------------------------★★★★ </br>" + e + "</font>");
                fail++;
            }

        }
        self.display && (self.display.innerHTML += "<h4> total: " + tests.cases.length + " cases, success: " + sucess + " cases, fail: " + fail + " cases </h5></br>");
    };
    return self;
})();

export {elsaTest};