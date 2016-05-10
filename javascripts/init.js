var init = function() {
    var self = this;

    var app = require("biotea-vis-annotation");
    self.cloud = new app({
         el: '#visAnnot'
    });
    self.selectedContent = 'ta';
    self.selectedTopic = '_100';
    self.selectedArticle = 7866872;

    self.conSelect = undefined;
    self.topicsSelect = undefined;
    self.articlesSelect = undefined;

    self.contentOptions = undefined;
    self.topicsOption = undefined;
    self.articlesOption = undefined;

    self.start = function() {
        var controls = d3.select('#controls');

        var contentDiv = controls.append('div');
        contentDiv.append('span').text('Type of content: ');
        self.conSelect = contentDiv.append('span').append('select')
            .attr('id', 'contentSelection')
            .on('change', function() {
                var selectedIndex = self.conSelect.property('selectedIndex')
                self.selectedContent = self.contentOptions[0][selectedIndex].__data__.value;
                self.updateTopics();
            });
        self.contentOptions = self.conSelect.selectAll('option')
            .data(contentType)
            .enter().append('option')
            .attr('value', function(type) {return type.value;})
            .text(function(type) {return type.text;});
        self.conSelect.select('option').attr('selected', 'selected');

        var topicsDiv = controls.append('div');
        topicsDiv.append('span').text('TREC topic: ');
        self.topicsSelect = topicsDiv.append('span').append('select')
            .attr('id', 'topicsSelection')
            .on('change', function() {
                var selectedIndex = self.topicsSelect.property('selectedIndex')
                self.selectedTopic = self.topicsOption[0][selectedIndex].__data__.value;
                self.updateArticles(selectedIndex);
            });

        var articlesDiv = controls.append('div');
        articlesDiv.append('span').text('Articles: ');
        self.articlesSelect = articlesDiv.append('span').append('select')
            .attr('id', 'articlesSelection')
            .on('change', function() {
                var selectedIndex = self.articlesSelect.property('selectedIndex')
                var selectedArticle = self.articlesOption[0][selectedIndex].__data__;
                self.updateCloud(selectedArticle.id);
            });

        self.updateTopics();
    };

    self.updateTopics = function() {
        var topics;
        if (self.selectedContent === 'ta') {
            topics = pubmed_trecTopics;
        } else {
            topics = pmc_trecTopics;
        }

        self.topicsSelect.selectAll('option').remove();
        self.topicsOption = self.topicsSelect.selectAll('option')
            .data(topics)
            .enter().append('option')
            .attr('value', function(topic) {return topic.value;})
            .text(function(topic) {return topic.text;});

        self.topicsSelect.select('option').attr('selected', 'selected');
        var selectedArticle = self.updateArticles(0);
    };

    self.updateArticles = function(selectedIndex) {
        var topics, articles;
        if (selectedContent === 'ta') {
            topics = pubmed_trecTopics;
            articles = pubmed_articles;
        } else {
            topics = pmc_trecTopics;
            articles = pmc_articles;
        }

        var topicArticles = _.filter(articles, function(art) {
            return art.topic === topics[selectedIndex].value;
        });

        self.articlesSelect.selectAll('option').remove();
        self.articlesOption = self.articlesSelect.selectAll('option')
            .data(topicArticles)
            .enter().append('option')
            .attr('value', function(d) {return '_' + d.id;})
            .text(function(d) {return d.title;});

        self.articlesSelect.select('option').attr('selected', 'selected');
        self.updateCloud(topicArticles[0].id);
        return topicArticles[0];
    };

    self.updateCloud = function(id) {
        var path, articles;
        if (self.selectedContent === 'ta') {
            path = './pubmed/';
        } else {
            path = './pmc/'
        }
        self.cloud.load(path, id);
    }

    return self;
}();






