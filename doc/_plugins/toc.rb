#
# Copyright (c) 2011, salesforce.com, inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification, are permitted provided
# that the following conditions are met:
#
#    Redistributions of source code must retain the above copyright notice, this list of conditions and the
#    following disclaimer.
#
#    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
#    the following disclaimer in the documentation and/or other materials provided with the distribution.
#
#    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
#    promote products derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
# PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
# TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#

module Jekyll

  class CategoryIndex < Page
    def initialize(site, base, dir, category)
      puts "test2"
#      @site = site
#      @base = base
#      @dir = dir
#      @name = 'index.html'
#
#      self.process(@name)
#      self.read_yaml(File.join(base, '_layouts'), 'category_index.html')
#      self.data['category'] = category
#
#      category_title_prefix = site.config['category_title_prefix'] || 'Category: '
#      self.data['title'] = "#{category_title_prefix}#{category}"
    end
  end

  class CategoryGenerator < Generator
    safe true
    
    def generate(site)
      puts "test"
      site.pages.each { |p| 
        puts p.data['title']
        puts p.site
        puts p.name
        puts p.basename
        puts p.dir
        puts p.url
      }
#      if site.layouts.key? 'category_index'
#        dir = site.config['category_dir'] || 'categories'
#        site.categories.keys.each do |category|
#          write_category_index(site, File.join(dir, category), category)
#        end
#      end
    end
  
#    def write_category_index(site, dir, category)
#      index = CategoryIndex.new(site, site.source, dir, category)
#      index.render(site.layouts, site.site_payload)
#      index.write(site.dest)
#      site.pages << index
#    end
  end

  class RenderTimeTag < Liquid::Tag

      def initialize(tag_name, text, tokens)
        super
        @text = text
      end

      def render(context)
        puts "Context public methods ======================"
        puts context.registers[:site].pages
#        puts context.scopes
#        puts context.registers
        context.keys.each { |k| 
          puts "#{k}: #{context[k]}"
        }
        puts "======================"
        "#{@text} #{Time.now}"
      end
    end
end

Liquid::Template.register_tag('render_time', Jekyll::RenderTimeTag)

